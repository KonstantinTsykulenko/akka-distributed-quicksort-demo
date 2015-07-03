package com.tsykul.forkjoin.distributed.common.messages

import java.util.UUID

import com.tsykul.forkjoin.distributed.common.task.Task


case class Work[WORK, RESULT](w: WORK, t: Task[WORK, RESULT], uid: String = UUID.randomUUID().toString, parentUid: String = null)