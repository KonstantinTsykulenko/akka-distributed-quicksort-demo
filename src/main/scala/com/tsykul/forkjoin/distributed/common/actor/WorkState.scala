package com.tsykul.forkjoin.distributed.common.actor

sealed trait WorkState

case object Pending extends WorkState
case object Scheduled extends WorkState
case object Accepted extends WorkState
case object Completed extends WorkState

