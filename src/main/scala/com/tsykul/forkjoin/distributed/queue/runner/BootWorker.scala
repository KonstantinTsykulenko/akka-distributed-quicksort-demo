package com.tsykul.forkjoin.distributed.queue.runner

import akka.actor.{Props, ActorSystem}
import akka.contrib.pattern.ClusterSingletonProxy
import com.tsykul.forkjoin.distributed.queue.actors.Worker
import com.typesafe.config.ConfigFactory

object BootWorker extends App {
  val factory = ActorSystem("DistWorkers", ConfigFactory.load("worker"))
  val workQueue = factory.actorOf(ClusterSingletonProxy.props(
    singletonPath = "/user/singleton/work-queue",
    role = Some("work-queue")),
    name = "work-queue-proxy")
  val worker = factory.actorOf(Props(classOf[Worker[_, _]], workQueue))
}
