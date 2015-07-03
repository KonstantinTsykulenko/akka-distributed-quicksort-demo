package com.tsykul.forkjoin.distributed.common.messages

case class WorkResult[RESULT](res: RESULT, uid: String, parentUid: String = null)
