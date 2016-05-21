package scorex.transaction.proof

import scorex.crypto.signatures.Curve25519

/**
  * @param signature 25519 signature
  */
case class Signature25519(signature: Array[Byte]) extends Proof {
  override def proofId: Byte = 100: Byte

  override def proofBytes: Array[Byte] = signature
}

object Signature25519 {
  lazy val SignatureSize = Curve25519.SignatureLength25519
}