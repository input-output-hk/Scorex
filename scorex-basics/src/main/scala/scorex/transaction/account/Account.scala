package scorex.transaction.account

import scorex.crypto.encode.Base58
import scorex.crypto.hash.FastCryptographicHash._
import scorex.transaction.state.StateElement


abstract class Account(val publicKey: Array[Byte]) extends StateElement {

  val AddressVersion: Byte = 1
  val ChecksumLength = 4
  val HashLength = 20
  val AddressLength = 1 + ChecksumLength + HashLength

  lazy val address: String = {
    val publicKeyHash = hash(publicKey).take(HashLength)
    val withoutChecksum = AddressVersion +: publicKeyHash //prepend ADDRESS_VERSION
    Base58.encode(withoutChecksum ++ calcCheckSum(withoutChecksum))
  }

  override lazy val bytes = Base58.decode(address).get

  override def toString: String = address

  override def equals(obj: Any): Boolean = obj match {
    case acc: Account => acc.address == this.address
    case _ => false
  }

  private def calcCheckSum(withoutChecksum: Array[Byte]): Array[Byte] = hash(withoutChecksum).take(ChecksumLength)

  override def hashCode(): Int = address.hashCode()
}


object Account {

  /**
    * Create account from public key. Used in PublicKeyAccount/PrivateKeyAccount.
    */
  def isValidAddress(address: String): Boolean =
    Base58.decode(address).map { addressBytes =>
      if (addressBytes.length != Account.AddressLength)
        false
      else {
        val checkSum = addressBytes.takeRight(ChecksumLength)

        val checkSumGenerated = calcCheckSum(addressBytes.dropRight(ChecksumLength))

        checkSum.sameElements(checkSumGenerated)
      }
    }.getOrElse(false)
}
