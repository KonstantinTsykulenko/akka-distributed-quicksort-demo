package com.tsykul.forkjoin.distributed.common.actor

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Subscribe
import com.tsykul.forkjoin.distributed.common.messages.{Work, WorkResult}
import com.tsykul.forkjoin.distributed.common.task.SortTask

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

class Client(val dispatcher: ActorRef) extends Actor with ActorLogging {

  val mediator =
    DistributedPubSubExtension.get(context.system).mediator

  override def preStart(): Unit = {

    context.system.scheduler.scheduleOnce(FiniteDuration(5, TimeUnit.SECONDS), self, Work(Seq.fill(8)(Random.nextInt(100)).toList, new SortTask))(context.dispatcher)
  }

  override def receive: Receive = {
    case w: Work[_, _]=>
      log.debug("Produced work: {}", w)
      mediator ! Subscribe(s"workCompletion-${w.uid}", self)
      dispatcher ! w
    case wr: WorkResult[_] => log.info("Work result: {}", wr)
  }
}