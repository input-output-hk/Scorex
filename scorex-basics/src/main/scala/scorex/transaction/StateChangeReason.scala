package scorex.transaction

/**
  * A reason to change a system state
  */
trait StateChangeReason {
  /**
    * A reason could be serialized into a binary form
    */
  val bytes: Array[Byte]

  val serializedProof: Array[Byte]
}
