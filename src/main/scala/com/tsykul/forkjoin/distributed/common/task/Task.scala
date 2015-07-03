package com.tsykul.forkjoin.distributed.common.task

import com.tsykul.forkjoin.distributed.common.messages.Work

import scala.concurrent.Future

trait Task[WORK, RESULT] {
 def exec(w: Work[WORK, RESULT], tc: TaskContext[WORK, RESULT]): Future[RESULT]
}
