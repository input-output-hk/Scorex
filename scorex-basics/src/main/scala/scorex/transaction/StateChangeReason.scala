package scorex.transaction


/**
  * A proof
  */

trait Proof {
  val bytes: Array[Byte]
}

case class Signature(signature: Array[Byte]) extends Proof{
  override val bytes: Array[Byte] = Array()
}

case object NoProof extends Proof {
  override val bytes: Array[Byte] = Array()
}

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