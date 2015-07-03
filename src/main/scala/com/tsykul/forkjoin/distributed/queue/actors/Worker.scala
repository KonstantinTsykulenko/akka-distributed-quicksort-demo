package com.tsykul.forkjoin.distributed.queue.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator._
import akka.event.LoggingAdapter
import com.tsykul.forkjoin.distributed.common.messages.{Work, WorkResult}
import com.tsykul.forkjoin.distributed.common.task.TaskContext
import com.tsykul.forkjoin.distributed.queue.messages.WorkerReady

import scala.concurrent.{Future, Promise}

class Worker[WORK, RESULT](val master: ActorRef) extends Actor with ActorLogging with TaskContext[WORK, RESULT] {

  val mediator =
    DistributedPubSubExtension.get(context.system).mediator

  override implicit val ec = context.dispatcher

  override def logger: LoggingAdapter = log

  val workMap = scala.collection.mutable.Map[String, Promise[RESULT]]()

  override def preStart() = {
    master ! WorkerReady
  }

  override def receive: Receive = {
    case w: Work[WORK, RESULT] =>
      log.info("Starting work: {}", w)
      w.t.exec(w, this).map(res => {
        log.info("Got result: {}", res)
        WorkResult(res, w.uid, w.parentUid)
      }).onSuccess({ case wr: WorkResult[RESULT] =>
          mediator ! Publish(s"workCompletion-${wr.uid}", wr)
          if (null == wr.parentUid)
            master ! wr
      })
      master ! WorkerReady
    case wr: WorkResult[RESULT] =>
      workMap(wr.uid).success(wr.res)
      mediator ! Unsubscribe(s"workCompletion-${wr.uid}", self)
  }

  override def fork(w: Work[WORK, RESULT]): Future[RESULT] = {
    val p = Promise[RESULT]()
    master ! w
    workMap += w.uid -> p
    mediator ! Subscribe(s"workCompletion-${w.uid}", self)
    p.future
  }

}
