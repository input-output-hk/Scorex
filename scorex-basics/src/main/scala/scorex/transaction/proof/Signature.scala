package scorex.transaction.proof

case class Signature(signature: Array[Byte]) extends Proof {
  override lazy val bytes: Array[Byte] = signature
}




