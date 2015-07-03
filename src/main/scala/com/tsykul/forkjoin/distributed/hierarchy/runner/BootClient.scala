package com.tsykul.forkjoin.distributed.hierarchy.runner

import akka.actor.{ActorSystem, Props}
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import akka.routing.ConsistentHashingGroup
import com.tsykul.forkjoin.distributed.common.actor.Client
import com.tsykul.forkjoin.distributed.common.messages.WorkMapper
import com.typesafe.config.ConfigFactory

object BootClient extends App {
  val factory = ActorSystem("DistWorkers", ConfigFactory.load("dist-workers-client"))

  val workers = List("/user/master")

  val dispatcher = factory.actorOf(
    ClusterRouterGroup(
      ConsistentHashingGroup(Nil).withHashMapper(WorkMapper()),
      ClusterRouterGroupSettings(Int.MaxValue, workers, allowLocalRoutees = true, Option("worker"))
    ).props())

    val client = factory.actorOf(Props(classOf[Client], dispatcher), "client")
}
