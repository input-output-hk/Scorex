package scorex.transaction

import java.util

import com.google.common.primitives.{Bytes, Ints, Longs}
import play.api.libs.json.{JsObject, Json}
import scorex.account.{Account, PrivateKeyAccount, PublicKeyAccount}
import scorex.crypto.EllipticCurveImpl
import scorex.crypto.encode.Base58
import scorex.serialization.Deser
import scorex.transaction.LagonakiTransaction.TransactionType

import scala.util.{Failure, Try}

@SerialVersionUID(-4989881425715590828L)
class PaymentTransaction(val sender: PublicKeyAccount,
                         override val recipient: Account,
                         override val amount: Long,
                         override val fee: Long,
                         override val timestamp: Long,
                         val attachment: Array[Byte],
                         override val signature: Array[Byte])
  extends LagonakiTransaction(TransactionType.PaymentTransaction, recipient, amount, fee, timestamp, signature) {

  import scorex.transaction.LagonakiTransaction._
  import scorex.transaction.PaymentTransaction._

  override lazy val dataLength = TypeLength + BaseLength

  override lazy val creator = Some(sender)

  override lazy val json: JsObject = jsonBase() ++ Json.obj(
    "sender" -> sender.address,
    "recipient" -> recipient.address,
    "amount" -> amount,
    "attachment" -> Base58.encode(attachment)
  )

  override lazy val bytes: Array[Byte] = {
    def packAttachment(): Array[Byte] = if (attachment.length == 0) {
      Array.empty
    } else {
      val sliced = attachment.take(MaxAttachmentSize)
      Bytes.concat(Ints.toByteArray(sliced.length), sliced)
    }
    val typeBytes = Array(TypeId.toByte)

    val timestampBytes = Longs.toByteArray(timestamp)
    val amountBytes = Longs.toByteArray(amount)
    val feeBytes = Longs.toByteArray(fee)

    Bytes.concat(typeBytes, timestampBytes, sender.publicKey,
      Base58.decode(recipient.address).get, amountBytes,
      feeBytes, signature, packAttachment())
  }

  override lazy val signatureValid: Boolean = {
    val data = signatureData(sender, recipient, amount, fee, timestamp, attachment)
    EllipticCurveImpl.verify(signature, data, sender.publicKey)
  }

  override def validate: ValidationResult.Value =
    if (!Account.isValidAddress(recipient.address)) {
      ValidationResult.InvalidAddress //CHECK IF RECIPIENT IS VALID ADDRESS
    } else if (amount <= 0) {
      ValidationResult.NegativeAmount //CHECK IF AMOUNT IS POSITIVE
    } else if (fee <= 0) {
      ValidationResult.NegativeFee //CHECK IF FEE IS POSITIVE
    } else ValidationResult.ValidateOke


  override def involvedAmount(account: Account): Long = {
    val address = account.address

    if (address.equals(sender.address) && address.equals(recipient.address)) {
      -fee
    } else if (address.equals(sender.address)) {
      -amount - fee
    } else if (address.equals(recipient.address)) {
      amount
    } else 0
  }

  override def balanceChanges(): Seq[(Account, Long)] =
    Seq((sender, -amount - fee), (recipient, amount))
}

object PaymentTransaction extends Deser[PaymentTransaction] {

  import scorex.transaction.LagonakiTransaction._

  val MaxAttachmentSize = 4096
  private val SenderLength = 32
  private val FeeLength = 8
  private val SignatureLength = 64
  private val BaseLength = TimestampLength + SenderLength + RecipientLength + AmountLength + FeeLength + SignatureLength

  def apply(sender: PrivateKeyAccount, recipient: Account,
            amount: Long, fee: Long, timestamp: Long, attachment: Array[Byte]): PaymentTransaction = {
    val sig = generateSignature(sender, recipient, amount, fee, timestamp, attachment)
    new PaymentTransaction(sender, recipient, amount, fee, timestamp, attachment, sig)
  }

  def parseBytes(data: Array[Byte]): Try[PaymentTransaction] = {
    data.head match {
      case transactionType: Byte if transactionType == TransactionType.PaymentTransaction.id =>
        parseTail(data.tail)
      case transactionType =>
        Failure(new Exception(s"Incorrect transaction type '$transactionType' in PaymentTransaction data"))
    }
  }

  def parseTail(data: Array[Byte]): Try[PaymentTransaction] = Try {
    require(data.length >= BaseLength, "Data does not match base length")

    var position = 0

    //READ TIMESTAMP
    val timestampBytes = data.take(TimestampLength)
    val timestamp = Longs.fromByteArray(timestampBytes)
    position += TimestampLength

    //READ SENDER
    val senderBytes = util.Arrays.copyOfRange(data, position, position + SenderLength)
    val sender = new PublicKeyAccount(senderBytes)
    position += SenderLength

    //READ RECIPIENT
    val recipientBytes = util.Arrays.copyOfRange(data, position, position + RecipientLength)
    val recipient = new Account(Base58.encode(recipientBytes))
    position += RecipientLength

    //READ AMOUNT
    val amountBytes = util.Arrays.copyOfRange(data, position, position + AmountLength)
    val amount = Longs.fromByteArray(amountBytes)
    position += AmountLength

    //READ FEE
    val feeBytes = util.Arrays.copyOfRange(data, position, position + FeeLength)
    val fee = Longs.fromByteArray(feeBytes)
    position += FeeLength

    //READ SIGNATURE
    val signatureBytes = util.Arrays.copyOfRange(data, position, position + SignatureLength)
    position += SignatureLength

    val attachmentLength = Try(Ints.fromByteArray(util.Arrays.copyOfRange(data, position, position + 4))).getOrElse(0)
    val attachment: Array[Byte] = if (attachmentLength > 0) {
      position += 4
      util.Arrays.copyOfRange(data, position, position + attachmentLength)
    } else Array.empty

    new PaymentTransaction(sender, recipient, amount, fee, timestamp, attachment, signatureBytes)
  }

  def generateSignature(sender: PrivateKeyAccount, recipient: Account,
                        amount: Long, fee: Long, timestamp: Long, attachment: Array[Byte]): Array[Byte] = {
    EllipticCurveImpl.sign(sender, signatureData(sender, recipient, amount, fee, timestamp, attachment))
  }

  private def signatureData(sender: PublicKeyAccount, recipient: Account,
                            amount: Long, fee: Long, timestamp: Long, attachment: Array[Byte]): Array[Byte] = {
    val typeBytes = Ints.toByteArray(TransactionType.PaymentTransaction.id)
    val timestampBytes = Longs.toByteArray(timestamp)
    val amountBytes = Longs.toByteArray(amount)
    val feeBytes = Longs.toByteArray(fee)

    Bytes.concat(typeBytes, timestampBytes, sender.publicKey,
      Base58.decode(recipient.address).get, amountBytes, feeBytes, attachment)
  }
}
