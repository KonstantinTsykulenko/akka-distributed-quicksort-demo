package com.tsykul.forkjoin.distributed.queue.runner

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.contrib.pattern.{ClusterSingletonManager, ClusterSingletonProxy}
import com.tsykul.forkjoin.distributed.common.actor.Client
import com.tsykul.forkjoin.distributed.queue.actors.WorkQueue
import com.typesafe.config.ConfigFactory

object BootQueueAndClient extends App {
  val factory = ActorSystem("DistWorkers", ConfigFactory.load("work-queue"))
  val workQueue = factory.actorOf(ClusterSingletonManager.props(
    singletonProps = Props[WorkQueue],
    singletonName = "work-queue",
    terminationMessage = PoisonPill,
    role = Some("work-queue")),
    name = "singleton")
  val workQueueProxy = factory.actorOf(ClusterSingletonProxy.props(
    singletonPath = "/user/singleton/work-queue",
    role = Some("work-queue")),
    name = "work-queue-proxy")
  val client = factory.actorOf(Props(classOf[Client], workQueueProxy))
}