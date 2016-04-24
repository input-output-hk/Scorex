package scorex.transaction

import scorex.transaction.proof.Proof

/**
  * A reason to change a system state
  */
trait StateChangeReason extends Serializable {
  /**
    * A reason could be serialized into a binary form
    */
  val bytes: Array[Byte]

  val proof: Proof
}