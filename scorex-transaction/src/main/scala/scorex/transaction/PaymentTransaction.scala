package scorex.transaction

import java.util

import com.google.common.primitives.{Bytes, Ints, Longs}
import play.api.libs.json.{JsObject, Json}
import scorex.crypto.encode.Base58
import scorex.serialization.BytesParseable
import scorex.transaction.LagonakiTransaction.TransactionType
import scorex.transaction.account.PublicKey25519NoncedBox
import scorex.transaction.box.{Box, PublicKeyProposition, PublicKey25519Proposition}
import scala.util.Try

case class PaymentTransaction(override val sender: PublicKey25519NoncedBox,
                              override val recipient: PublicKey25519NoncedBox,
                              override val txnonce : Int,
                              override val amount: Long,
                              override val fee: Long,
                              override val timestamp: Long,
                              override val signature: Array[Byte])
  extends LagonakiTransaction(TransactionType.PaymentTransaction, sender, recipient, txnonce, amount, fee, timestamp, signature) {

  import scorex.transaction.LagonakiTransaction._
  import scorex.transaction.PaymentTransaction._

  override lazy val dataLength = TypeLength + BaseLength

  override lazy val json: JsObject = jsonBase() ++ Json.obj(
    "sender" -> sender.address,
    "recipient" -> recipient.address,
    "amount" -> amount
  )

  override lazy val messageToSign: Array[Byte] = {
    val typeBytes = Array(TransactionType.PaymentTransaction.id.toByte)

    val timestampBytes = Longs.toByteArray(timestamp)
    val amountBytes = Longs.toByteArray(amount)
    val feeBytes = Longs.toByteArray(fee)

    Bytes.concat(typeBytes, timestampBytes, sender.publicKey,
      Base58.decode(recipient.address).get, amountBytes,
      feeBytes)
  }

  /*
  override def validate: ValidationResult.Value =
    if (!PublicKeyProposition.isValidAddress(recipient.address)) {
      ValidationResult.InvalidAddress //CHECK IF RECIPIENT IS VALID ADDRESS
    } else if (amount <= 0) {
      ValidationResult.NegativeAmount //CHECK IF AMOUNT IS POSITIVE
    } else if (fee <= 0) {
      ValidationResult.NegativeFee //CHECK IF FEE IS POSITIVE
    } else ValidationResult.ValidateOke


  override def involvedAmount(account: PublicKey25519Proposition): Long = {
    val address = account.address

    if (address.equals(sender.address) && address.equals(recipient.address)) {
      -fee
    } else if (address.equals(sender.address)) {
      -amount - fee
    } else if (address.equals(recipient.address)) {
      amount
    } else 0
  }

  override def balanceChanges(): Seq[(PublicKey25519Proposition, Long)] =
    Seq((sender, -amount - fee), (recipient, amount))*/
}

object PaymentTransaction extends BytesParseable[PaymentTransaction] {

  import scorex.transaction.LagonakiTransaction._

  private val SenderLength = 32
  private val FeeLength = 8
  private val SignatureLength = 64
  private val BaseLength = TimestampLength + SenderLength + RecipientLength + AmountLength + FeeLength + SignatureLength

  def apply(sender: PublicKey25519Proposition, recipient: PublicKey25519Proposition,
            txnonce: Int, amount: Long, fee: Long, timestamp: Long): PaymentTransaction = {
    val sig = generateSignature(sender, recipient, txnonce, amount, fee, timestamp)
    PaymentTransaction(sender, recipient, txnonce, amount, fee, timestamp, sig)
  }

  def parseBytes(data: Array[Byte]): Try[PaymentTransaction] = Try {
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

    PaymentTransaction(sender, recipient, amount, fee, timestamp, signatureBytes)
  }

  def generateSignature(sender: PrivateKeyAccount, recipient: PublicKeyProposition,
                        amount: Long, fee: Long, timestamp: Long): Array[Byte] = {
    EllipticCurveImpl.sign(sender, signatureData(sender, recipient, amount, fee, timestamp))
  }

  private def signatureData(sender: PublicKey25519Proposition, recipient: PublicKey25519Proposition,
                            amount: Long, fee: Long, timestamp: Long): Array[Byte] = {
    val typeBytes = Ints.toByteArray(TransactionType.PaymentTransaction.id)
    val timestampBytes = Longs.toByteArray(timestamp)
    val amountBytes = Longs.toByteArray(amount)
    val feeBytes = Longs.toByteArray(fee)

    Bytes.concat(typeBytes, timestampBytes, sender.publicKey,
      Base58.decode(recipient.address).get, amountBytes, feeBytes)
  }
}
