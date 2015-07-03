package com.tsykul.forkjoin.distributed.common.task

import com.tsykul.forkjoin.distributed.common.messages.Work

import scala.concurrent.{ExecutionContext, Future}

trait TaskContext[WORK, RESULT] {
  def fork(w: Work[WORK, RESULT]): Future[RESULT]

  def logger: akka.event.LoggingAdapter

  implicit val ec: ExecutionContext
}
