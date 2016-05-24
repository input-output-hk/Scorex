package scorex.transaction.account

import scorex.crypto.signatures.Curve25519

case class PubKey25519Account(override val publicKey: Array[Byte]) extends PublicKeyAccount(publicKey) {

  override def verify(message: Array[Byte], signature: Array[Byte], nonce: Array[Byte]): Boolean =
    new Curve25519().verify(signature, message, publicKey)
}
