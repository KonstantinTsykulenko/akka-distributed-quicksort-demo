package com.tsykul.forkjoin.distributed.hierarchy.runner

import akka.actor.{ActorSystem, Props}
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import akka.routing.ConsistentHashingGroup
import com.tsykul.forkjoin.distributed.common.messages.WorkMapper
import com.tsykul.forkjoin.distributed.hierarchy.actor.WorkerMaster
import com.typesafe.config.ConfigFactory

object BootWorker extends App {
  val factory = ActorSystem("DistWorkers", ConfigFactory.load("dist-workers"))

  val workers = List("/user/master")

  val dispatcher = factory.actorOf(
    ClusterRouterGroup(
      ConsistentHashingGroup(Nil).withHashMapper(WorkMapper()),
      ClusterRouterGroupSettings(Int.MaxValue, workers, allowLocalRoutees = true, Option("worker"))
    ).props())

  val master = factory.actorOf(Props(classOf[WorkerMaster], dispatcher), "master")
}
