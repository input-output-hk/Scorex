package scorex.transaction.proof

import scorex.crypto.signatures.Curve25519


case class Signature25519(signature: Array[Byte]) extends Proof {
  override lazy val bytes: Array[Byte] = signature
}

object Signature25519 {
  lazy val SignatureSize = new Curve25519().SignatureLength
}




