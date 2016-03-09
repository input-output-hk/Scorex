package scorex.transaction.box

import scorex.transaction.StateElement

/**
  * Box is a state element. Basically it is some value locked by some state machine.
  */
trait Box[L <: Lock] extends StateElement {
  require(value > 0)

  val SMin = 0 //todo: min box size

  val lock: L

  val ol: HeightOpenLock

  val bytes: Array[Byte]

  def minFee(currentHeight: Int): Int = (bytes.length - SMin + 1) * (ol.height - currentHeight)

  val memo: Array[Byte]
  val value: Long
}