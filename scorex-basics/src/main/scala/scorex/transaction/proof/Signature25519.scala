package scorex.transaction.proof

import scorex.crypto.signatures.Curve25519

/**
  * Special case to not rewrite a lot of code in SimnplestTransactionModule
  * Please note byte serialization is different from all other proofs
  * So it could not be used in conjuctions with other proofs
  * @param signature 25519 signature
  */
case class Signature25519(signature: Array[Byte]) extends Proof {
  override def proofId: Byte = 100: Byte

  override def proofBytes: Array[Byte] = signature
}

object Signature25519 {
  lazy val SignatureSize = new Curve25519().SignatureLength
}




