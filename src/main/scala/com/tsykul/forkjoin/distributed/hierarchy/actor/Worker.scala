package com.tsykul.forkjoin.distributed.hierarchy.actor

import java.util.concurrent.TimeUnit

import akka.actor._
import com.tsykul.forkjoin.distributed.common.actor.{Completed, Accepted, Scheduled, WorkState}
import com.tsykul.forkjoin.distributed.common.messages._
import com.tsykul.forkjoin.distributed.common.task.TaskContext

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}
import akka.pattern.pipe

class Worker[WORK, RESULT](val parentWorker: ActorRef, val dispatcher: ActorRef) extends Actor with ActorLogging with TaskContext[WORK, RESULT] {

  val workMap = scala.collection.mutable.Map[String, (Work[WORK, RESULT], Promise[RESULT], WorkState)]()
  val workerMap = scala.collection.mutable.Map[ActorRef, Work[WORK, RESULT]]()

  override implicit val ec = context.dispatcher

  override def logger = log

  override def receive: Receive = {
    case work: Work[WORK, RESULT] =>
      log.debug("Received work: {}", work)
      sender ! WorkAccepted(work.uid)
      work.t.exec(work, this).map(WorkResult(_, work.uid)).pipeTo(parentWorker)
    case workResult: WorkResult[RESULT] =>
      val (work, promise, state) = workMap(workResult.uid)
      if (!promise.isCompleted) {
        log.debug("Work completed: {}", workResult)
        promise.success(workResult.res)
        workMap += workResult.uid ->(work, promise, Completed)
      }
      sender ! PoisonPill
    case WorkAccepted(uid) =>
      val worker = sender()
      log.debug("Work accepted: {}, worker: {}", uid, worker)
      context.watch(worker)
      val (work, promise, state) = workMap(uid)
      workerMap += worker -> work
      workMap += uid ->(work, promise, Accepted)
    case Terminated(actor) =>
      val work = workerMap(actor)
      val (_, promise, state) = workMap(work.uid)
      if (state != Completed) {
        log.warning("Worker crashed, recovering {}", work)
        workMap += work.uid ->(work, promise, Scheduled)
        workerMap -= actor
        dispatcher ! work
        context.system.scheduler.scheduleOnce(FiniteDuration(15, TimeUnit.SECONDS), self, WorkTimeout(work.uid))
      }
    case WorkTimeout(uid) =>
      val (work, promise, state) = workMap(uid)
      if (!(state == Accepted || state == Completed)) {
        log.warning("Work not accepted by worker, rescheduling: {}", work)
        workMap += work.uid ->(work, promise, Scheduled)
        dispatcher ! work
        context.system.scheduler.scheduleOnce(FiniteDuration(15, TimeUnit.SECONDS), self, WorkTimeout(work.uid))
      }
  }

  override def fork(w: Work[WORK, RESULT]): Future[RESULT] = {
    context.system.scheduler.scheduleOnce(FiniteDuration(15, TimeUnit.SECONDS), self, WorkTimeout(w.uid))
    val p = Promise[RESULT]()
    workMap += w.uid ->(w, p, Scheduled)
    dispatcher ! w
    p.future
  }
}
