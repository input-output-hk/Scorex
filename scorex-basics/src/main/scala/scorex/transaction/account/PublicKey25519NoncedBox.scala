package scorex.transaction.account

import com.google.common.primitives.{Ints, Longs}
import scorex.crypto.signatures.Curve25519
import scorex.transaction.box.{SizedConstants, PublicKey25519Proposition}
import shapeless.Sized

case class PublicKey25519NoncedBox(
                               pubKey: Sized[Array[Byte], SizedConstants.PubKey25519],
                               override val nonce: Int,
                               override val value: Long
                             ) extends PublicKeyNoncedBox[PublicKey25519Proposition] {

  val lock = new PublicKey25519Proposition{
    override val publicKey = pubKey
    override val bytes: Array[Byte] = publicKey
  }

  def verify(message: Array[Byte], signature: Sized[Array[Byte], SizedConstants.Signature25519], nonce: Array[Byte]): Boolean =
    Curve25519.verify(signature, message, publicKey)

  override def bytes: Array[Byte] = pubKey.unsized ++ Ints.toByteArray(nonce) ++ Longs.toByteArray(value)
}