package scorex.transaction

import scorex.serialization.BytesSerializable

import scorex.transaction.proof.Proof

/**
  * A reason to change a system state
  */
trait StateChangeReason extends BytesSerializable {
  /**
    * A reason could be serialized into a binary form
    */
  val bytes: Array[Byte]

  val proof: Proof
}