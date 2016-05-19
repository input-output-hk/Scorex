package scorex.transaction.proof.special

import scorex.crypto.signatures.Curve25519
import scorex.transaction.proof.Proof

/**
  * Special case to not rewrite a lot of code in SimnplestTransactionModule
  * Please note byte serialization is different from all other proofs
  * So it could not be used in conjuctions with other proofs
  * @param signature 25519 signature
  */
case class Signature25519(signature: Array[Byte]) extends Proof {
  override val proofId: Byte = ???

  override val proofBytes: Array[Byte] = ???

  override lazy val bytes: Array[Byte] = signature
}

object Signature25519 {
  lazy val SignatureSize = new Curve25519().SignatureLength
}




