package scorex.transaction.box

import scorex.transaction.StateChangeReason
import scorex.transaction.proof.Proof

trait BoxUnlocker[P <: Proposition] extends StateChangeReason {
  val closedBoxId: Array[Byte]
  val boxKey: Proof[P]
}