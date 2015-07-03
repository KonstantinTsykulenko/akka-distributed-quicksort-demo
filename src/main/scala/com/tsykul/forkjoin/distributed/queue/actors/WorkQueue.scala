package com.tsykul.forkjoin.distributed.queue.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Terminated}
import akka.persistence.PersistentActor
import com.tsykul.forkjoin.distributed.common.messages.{Work, WorkResult}
import com.tsykul.forkjoin.distributed.queue.messages.WorkerReady

class WorkQueue extends Actor with PersistentActor with ActorLogging {

  val workQueue = scala.collection.mutable.Queue[Work[_, _]]()
  val freeWorkers = scala.collection.mutable.Queue[ActorRef]()
  val workerMap = scala.collection.mutable.Map[ActorRef, Work[_, _]]()

  override def receiveRecover: Receive = {
    case w: Work[_, _] => workQueue += w
    case wr: WorkResult[_] => workQueue.dequeueFirst(_.uid == wr.uid)
  }

  override def receiveCommand: Receive = {
    case w: Work[_, _] =>
      if (null != w.parentUid)
        processWork(w)
      else
        persist(w)(_ => {
          processWork(w)
        })
    case wr@WorkerReady =>
      freeWorkers += sender()
      if (workQueue.nonEmpty) {
        dispatchWork()
      }
    case wr: WorkResult[_] =>
      persist(wr)(_ => {
        workerMap -= sender
        context.unwatch(sender)
      })
    case Terminated(actor) =>
      val work = workerMap(actor)
      processWork(work)
  }

  private def processWork(w: Work[_, _]): Unit = {
    workQueue += w
    if (freeWorkers.nonEmpty) {
      dispatchWork()
    }
  }

  private def dispatchWork(): Unit = {
    val work = workQueue.dequeue()
    val worker = freeWorkers.dequeue()
    context.watch(worker)
    workerMap += worker -> work
    worker ! work
  }

  override def persistenceId: String = "work-queue"
}
