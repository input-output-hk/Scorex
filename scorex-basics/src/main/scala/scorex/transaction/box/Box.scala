package scorex.transaction.box

import scorex.transaction.state.StateElement

/**
  * Box is a state element. Basically it is some value locked by some condition.
  */
trait Box[L <: Proposition] extends StateElement {
  type Id

  val id: Id

  val lock: L

  val bytes: Array[Byte]

  val SMin = 0
  //todo: min box size
  val fee: Int

  val value: Long
  require(value > 0)
}

//todo: move SigmaBox/ErgakiBox to the SigmaCoin module
trait SigmaBox[SL <: SigmaProposition] extends Box[SL]

trait ErgakiBox[SL <: SigmaProposition] extends SigmaBox[SL] {
  //garbage-collecting lock
  val gcLock: HeightOpenProposition

  def minFee(currentHeight: Int): Int =
    (bytes.length - SMin + 1) * (gcLock.height - currentHeight)

  val memo: Array[Byte]
}