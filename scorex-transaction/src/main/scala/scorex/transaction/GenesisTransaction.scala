package scorex.transaction

import com.google.common.primitives.{Bytes, Ints, Longs}
import play.api.libs.json.{JsObject, Json}
import scorex.crypto.encode.Base58
import scorex.crypto.hash.FastCryptographicHash._
import scorex.serialization.BytesParseable
import scorex.transaction.LagonakiTransaction.TransactionType
import scorex.transaction.box.{PublicKeyProposition, PublicKey25519Proposition}

import scala.util.Try


case class GenesisTransaction(override val recipients: Traversable[PublicKey25519Proposition],
                              override val amount: Long,
                              override val timestamp: Long)
  extends LagonakiTransaction(TransactionType.GenesisTransaction, recipients, amount, 0, timestamp,
    GenesisTransaction.generateSignature(recipients.head, amount, timestamp)) {

  import scorex.transaction.GenesisTransaction._
  import scorex.transaction.LagonakiTransaction._

  require(recipients.size == 1)

  lazy val recipient = recipients.head

  override lazy val senders: Option[PublicKey25519Proposition] = None

  override lazy val json: JsObject =
    jsonBase() ++ Json.obj("recipient" -> recipients.head.address, "amount" -> amount.toString)

  override lazy val messageToSign: Array[Byte] = {
    val typeBytes = Array(TransactionType.GenesisTransaction.id.toByte)

    val timestampBytes = Bytes.ensureCapacity(Longs.toByteArray(timestamp), TimestampLength, 0)

    val amountBytes = Bytes.ensureCapacity(Longs.toByteArray(amount), AmountLength, 0)

    val rcpBytes = Base58.decode(recipient.address).get
    require(rcpBytes.length == PublicKeyProposition.AddressLength)

    val res = Bytes.concat(typeBytes, timestampBytes, rcpBytes, amountBytes)
    require(res.length == dataLength)
    res
  }

  override lazy val dataLength = TypeLength + BASE_LENGTH

  /*
  lazy val correctAuthorship: Boolean = {
    val typeBytes = Bytes.ensureCapacity(Ints.toByteArray(TransactionType.GenesisTransaction.id), TypeLength, 0)
    val timestampBytes = Bytes.ensureCapacity(Longs.toByteArray(timestamp), TimestampLength, 0)
    val amountBytes = Bytes.ensureCapacity(Longs.toByteArray(amount), AmountLength, 0)
    val data = Bytes.concat(typeBytes, timestampBytes, Base58.decode(recipient.address).get, amountBytes)

    val h = hash(data)
    Bytes.concat(h, h).sameElements(signature)
  } */

  override def validate: ValidationResult.Value =
    if (amount < 0) {
      ValidationResult.NegativeAmount
    } else if (!PublicKeyProposition.isValidAddress(recipient.address)) {
      ValidationResult.InvalidAddress
    } else ValidationResult.ValidateOke

  override def involvedAmount(account: PublicKey25519Proposition): Long = if (recipient.address.equals(account.address)) amount else 0

  override def balanceChanges(): Seq[(PublicKey25519Proposition, Long)] = Seq((recipient, amount))
}


object GenesisTransaction extends BytesParseable[GenesisTransaction] {

  import scorex.transaction.LagonakiTransaction._

  private val RECIPIENT_LENGTH = PublicKeyProposition.AddressLength
  private val BASE_LENGTH = TimestampLength + RECIPIENT_LENGTH + AmountLength

  def generateSignature(recipient: PublicKey25519Proposition, amount: Long, timestamp: Long): Array[Byte] = {
    val typeBytes = Bytes.ensureCapacity(Ints.toByteArray(TransactionType.GenesisTransaction.id), TypeLength, 0)
    val timestampBytes = Bytes.ensureCapacity(Longs.toByteArray(timestamp), TimestampLength, 0)
    val amountBytes = Longs.toByteArray(amount)
    val amountFill = new Array[Byte](AmountLength - amountBytes.length)

    val data = Bytes.concat(typeBytes, timestampBytes,
      Base58.decode(recipient.address).get, Bytes.concat(amountFill, amountBytes))

    val h = hash(data)
    Bytes.concat(h, h)
  }

  def parseBytes(data: Array[Byte]): Try[GenesisTransaction] = Try {
    require(data.length >= BASE_LENGTH, "Data does not match base length")

    var position = 0

    val timestampBytes = java.util.Arrays.copyOfRange(data, position, position + TimestampLength)
    val timestamp = Longs.fromByteArray(timestampBytes)
    position += TimestampLength

    val recipientBytes = java.util.Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH)
    val recipient = new Account(Base58.encode(recipientBytes))
    position += RECIPIENT_LENGTH

    val amountBytes = java.util.Arrays.copyOfRange(data, position, position + AmountLength)
    val amount = Longs.fromByteArray(amountBytes)

    GenesisTransaction(recipient, amount, timestamp)
  }
}
