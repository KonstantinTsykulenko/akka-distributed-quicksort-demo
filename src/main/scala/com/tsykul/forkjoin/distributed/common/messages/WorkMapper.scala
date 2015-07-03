package com.tsykul.forkjoin.distributed.common.messages

import akka.routing.ConsistentHashingRouter.ConsistentHashMapper

class WorkMapper extends ConsistentHashMapper {
  override def hashKey(message: Any): Any = message.hashCode()
}

object WorkMapper {
  def apply() = new WorkMapper
}

