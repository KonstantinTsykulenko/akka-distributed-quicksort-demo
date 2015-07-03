package com.tsykul.forkjoin.distributed.common.task

import com.tsykul.forkjoin.distributed.common.messages.Work

import scala.concurrent.Future

@SerialVersionUID(100L)
class SortTask extends Task[List[Int], List[Int]] with Serializable {
  override def exec(w: Work[List[Int], List[Int]], tc: TaskContext[List[Int], List[Int]]): Future[List[Int]] = {
    import tc.ec
    Thread.sleep(5000)
    w.w match {
      case head :: tail =>
        val (lesser, greater) = tail.partition(_ < head)
        val left = tc.fork(Work(lesser, this, parentUid = w.uid))
        val right = tc.fork(Work(greater, this, parentUid = w.uid))
        for (leftRes <- left; rightRes <- right)
        yield leftRes ::: head :: rightRes
      case Nil =>
        Future.successful(Nil)
    }
  }
}
