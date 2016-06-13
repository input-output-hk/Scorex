package scorex.transaction.box

import com.google.common.primitives.Ints
import scorex.crypto.encode.Base58
import scorex.crypto.hash.FastCryptographicHash._
import scorex.serialization.BytesSerializable
import shapeless.{Succ, Sized, Nat}

sealed trait Proposition extends BytesSerializable

trait EmptyProposition extends Proposition

trait PublicKeyProposition extends Proposition {
  import PublicKeyProposition._

  type PublicKeySize <: Nat
  val publicKey: Sized[Array[Byte], PublicKeySize]

  override val bytes = publicKey.unsized

  lazy val address: String = Base58.encode(id ++ calcCheckSum(id))

  override def toString: String = address

  lazy val id = {
    val publicKeyHash = hash(publicKey).unsized.take(IdLength)
    AddressVersion +: publicKeyHash
  }
}

object PublicKeyProposition {
  val AddressVersion: Byte = 1
  val ChecksumLength = 4
  val IdLength = 20
  val AddressLength = 1 + IdLength + ChecksumLength

  //todo: unsized
  def calcCheckSum(bytes: Array[Byte]): Array[Byte] = hash(bytes).unsized.take(ChecksumLength)

  def isValidAddress(address: String): Boolean =
    Base58.decode(address).map { addressBytes =>
      if (addressBytes.length != AddressLength)
        false
      else {
        val checkSum = addressBytes.takeRight(ChecksumLength)

        val checkSumGenerated = calcCheckSum(addressBytes.dropRight(ChecksumLength))

        checkSum.sameElements(checkSumGenerated)
      }
    }.getOrElse(false)
}

trait PublicKey25519Proposition extends PublicKeyProposition {
  override type PublicKeySize = SizedConstants.Nat32
}

object SizedConstants {
  type Nat32 = Succ[Succ[Succ[Succ[Succ[Succ[Succ[Succ[Succ[Succ[Nat._22]]]]]]]]]]

  type Nat40 = Succ[Succ[Succ[Succ[Succ[Succ[Succ[Succ[Nat32]]]]]]]]

  type Nat50 = Succ[Succ[Succ[Succ[Succ[Succ[Succ[Succ[Succ[Succ[Nat40]]]]]]]]]]

  type Nat60 = Succ[Succ[Succ[Succ[Succ[Succ[Succ[Succ[Succ[Succ[Nat50]]]]]]]]]]

  type Nat64 = Succ[Succ[Succ[Succ[Nat60]]]]

  type PrivKey25519 = Nat32

  type PubKey25519 = Nat32

  type Blake2bDigestSize = Nat32

  type Signature25519 = Nat64
}

//todo: a_reserve
//todo: sigma protocol id
sealed trait SigmaProposition extends Proposition {
  val a: Array[Byte]
  val bytes = a
}

case class HeightOpenProposition(height: Int) extends Proposition {
  override val bytes = Ints.toByteArray(height)
}