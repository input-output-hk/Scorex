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

  def messageToSign: Array[Byte]

  val proof: Proof

  lazy val bytes: Array[Byte] = messageToSign ++ proof.bytes

  def correctAuthorship: Boolean
}