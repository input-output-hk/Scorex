package scorex.transaction.box

import scorex.transaction.state.StateElement

/**
  * Box is a state element. Basically it is some value locked by some state machine.
  */
trait Box[L <: Lock] extends StateElement {
  require(value > 0)

  val SMin = 0 //todo: min box size

  val lock: L

  //garbage-collecting lock
  val gcLock: HeightOpenLock

  val bytes: Array[Byte]

  def minFee(currentHeight: Int): Int =
    (bytes.length - SMin + 1) * (gcLock.height - currentHeight)

  val memo: Array[Byte]
  val value: Long
}