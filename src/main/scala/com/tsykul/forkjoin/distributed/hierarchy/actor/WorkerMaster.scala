package com.tsykul.forkjoin.distributed.hierarchy.actor

import akka.actor.{ActorRef, Props, Actor, ActorLogging}
import com.tsykul.forkjoin.distributed.common.messages.Work

class WorkerMaster(val dispatcher: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case w: Work[_, _] =>
      log.debug("Creating worker for work: {}", w)
      createWorker(sender()) forward w
  }

  private def createWorker(parent: ActorRef): ActorRef = {
    context.actorOf(Props(classOf[Worker[_, _]], parent, dispatcher))
  }
}
