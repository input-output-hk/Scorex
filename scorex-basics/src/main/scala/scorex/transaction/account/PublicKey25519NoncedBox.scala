package scorex.transaction.account

import scorex.crypto.signatures.Curve25519
import scorex.transaction.box.{SizedConstants, PublicKey25519Proposition}
import shapeless.Sized

case class PublicKey25519NoncedBox(
                               pubKey: Sized[Array[Byte], SizedConstants.PubKey25519],
                               override val nonce: Long,
                               override val value: Long
                             ) extends PublicKeyNoncedBox[PublicKey25519Proposition] {

  //todo: implement
  val lock = new PublicKey25519Proposition{
    override val publicKey = pubKey
    override val encodedType: Byte = ???
    override protected val specificBytes: Array[Byte] = ???
  }

  def verify(message: Array[Byte], signature: Sized[Array[Byte], SizedConstants.Signature25519], nonce: Array[Byte]): Boolean =
    new Curve25519().verify(signature, message, publicKey)

  override def bytes: Array[Byte] = ???
}