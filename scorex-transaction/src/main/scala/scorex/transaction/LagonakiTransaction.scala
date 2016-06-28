package scorex.transaction

import java.util

import com.google.common.primitives.{Bytes, Ints, Longs}
import scorex.crypto.encode.Base58
import scorex.serialization.{BytesParseable, BytesSerializable, JsonSerializable}
import scorex.transaction.LagonakiTransaction.TransactionType
import scorex.transaction.account.PublicKey25519NoncedBox
import scorex.transaction.box.{Box, PublicKey25519Proposition, PublicKeyProposition}
import scorex.transaction.proof.Signature25519
import scorex.transaction.state.{MinimalState, PrivateKey25519Holder}
import shapeless.Sized

import scala.util.{Failure, Success, Try}
import io.circe.syntax._

case class LagonakiTransaction(sender: PublicKey25519Proposition,
                               recipient: PublicKey25519Proposition,
                               txnonce: Int,
                               amount: Long,
                               override val fee: Long,
                               override val timestamp: Long,
                               signature: Signature25519)
  extends Transaction[PublicKey25519Proposition, LagonakiTransaction] with BytesSerializable with JsonSerializable {

  override def equals(other: Any): Boolean = other match {
    case tx: LagonakiTransaction => signature.signature.sameElements(tx.signature.signature)
    case _ => false
  }

  override def hashCode(): Int = Ints.fromByteArray(signature.signature)

  def genesisValidate(state: MinimalState[PublicKey25519Proposition, LagonakiTransaction]): Try[Unit] = Try {
    require(state.version == 0)
    require(sender.publicKey.unsized sameElements LagonakiTransaction.GodAccount.publicKey.unsized)
    require(timestamp == 0)
    require(fee == 0)
    require(txnonce == 0)
    require(amount > 0)
  }

  override def validate(state: MinimalState[PublicKey25519Proposition, LagonakiTransaction]): Try[Unit] = {
    if (state.version == 0) genesisValidate(state)
    else {
      state.closedBox(sender.id) match {
        case Some(box: PublicKey25519NoncedBox) =>
          lazy val feeValid = toTry(fee > 0, "Fee is not positive")
          lazy val balanceValid = toTry(box.value >= amount + fee, s"${sender.address} balance is too low")
          lazy val signatureValid = toTry(sender.verify(messageToSign, Sized.wrap(signature.signature)), "wrong signature")
          lazy val nonceValid = toTry(txnonce == box.nonce + 1, "tx nonce isnt' valid")

          nonceValid orElse balanceValid orElse signatureValid orElse feeValid
        case None => Failure(new Exception(s"No ${sender.address} found in state"))
      }
    }
  }

  def genesisChanges(): StateChanges[PublicKey25519Proposition] =
    StateChanges(Set(), Set(PublicKey25519NoncedBox(recipient, amount)), 0)

  override def changes(state: MinimalState[PublicKey25519Proposition, LagonakiTransaction])
  : Try[StateChanges[PublicKey25519Proposition]] = {
    if (state.version == 0) Success(genesisChanges())
    else {
      state.closedBox(sender.id) match {
        case Some(oldSender: PublicKey25519NoncedBox) => Success {
          val newSender = oldSender.copy(value = oldSender.value - amount - fee)

          val oldRcvrOpt = state.closedBox(recipient.id)
          val newRcvr = oldRcvrOpt.getOrElse {
            PublicKey25519NoncedBox(recipient, amount)
          }

          val toRemove: Set[Box[PublicKey25519Proposition]] = oldRcvrOpt match {
            case Some(oldRcvr) => Set(oldSender, oldRcvr)
            case None => Set(oldSender)
          }

          val toAppend = Set(newRcvr, newSender)

          StateChanges(toRemove, toAppend, fee)
        }
        case None => Failure(new Exception("Wrong kind of box"))
      }
    }
  }

  override lazy val messageToSign: Array[Byte] =
    LagonakiTransaction.bytesToSign(timestamp, sender.publicKey.unsized,
      recipient.publicKey.unsized, txnonce, amount, fee)

  override lazy val bytes = messageToSign ++ signature.signature

  override lazy val json =
    Map(
      "type" -> TransactionType.LagonakiTransaction.id.asJson,
      "fee" -> fee.asJson,
      "timestamp" -> timestamp.asJson,
      "nonce" -> txnonce.asJson,
      "signature" -> Base58.encode(this.signature.signature).asJson,
      "sender" -> sender.address.asJson,
      "recipient" -> recipient.address.asJson,
      "amount" -> amount.asJson
    ).asJson
}

object LagonakiTransaction extends BytesParseable[LagonakiTransaction] {

  private val SenderLength = 32
  private val RecipientLength = 32
  private val NonceLength = 8
  private val FeeLength = 8
  private val TimestampLength = 8
  private val AmountLength = 8
  private val SignatureLength = 64
  private val BaseLength = TimestampLength + SenderLength + RecipientLength + NonceLength + AmountLength + FeeLength + SignatureLength

  val GodAccount = PublicKey25519Proposition(Sized.wrap(Array.fill(SenderLength)(0: Byte)))

  object TransactionType extends Enumeration {
    val LagonakiTransaction = Value(1)
  }

  def apply(sender: PrivateKey25519Holder, recipient: PublicKey25519Proposition,
            txnonce: Int, amount: Long, fee: Long, timestamp: Long): LagonakiTransaction = {
    val sig = generateSignature(sender, recipient, txnonce, amount, fee, timestamp)
    LagonakiTransaction(sender.publicCommitment, recipient, txnonce, amount, fee, timestamp, sig)
  }


  def generateSignature(sender: PrivateKey25519Holder, recipient: PublicKeyProposition,
                        txNonce: Int, amount: Long, fee: Long, timestamp: Long): Signature25519 = {
    sender.sign(bytesToSign(timestamp, sender.publicCommitment.publicKey.unsized,
      recipient.publicKey.unsized, txNonce, amount, fee))
  }

  def parseBytes(data: Array[Byte]): Try[LagonakiTransaction] = Try {
    data.head match {
      case txType: Byte if txType == TransactionType.LagonakiTransaction.id =>
        require(data.length >= BaseLength, "Data does not match base length")

        var position = 0

        //READ TIMESTAMP
        val timestampBytes = data.take(TimestampLength)
        val timestamp = Longs.fromByteArray(timestampBytes)
        position += TimestampLength

        //READ SENDER
        val senderBytes = util.Arrays.copyOfRange(data, position, position + SenderLength)
        val sender = PublicKey25519Proposition(Sized.wrap(senderBytes))
        position += SenderLength

        //READ RECIPIENT
        val recipientBytes = util.Arrays.copyOfRange(data, position, position + RecipientLength)
        val recipient = PublicKey25519Proposition(Sized.wrap(recipientBytes))
        position += RecipientLength

        //READ NONCE
        val nonceBytes = util.Arrays.copyOfRange(data, position, position + NonceLength)
        val nonce = Ints.fromByteArray(nonceBytes)
        position += AmountLength

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

        new LagonakiTransaction(sender, recipient, nonce, amount, fee, timestamp, Signature25519(signatureBytes))

      case txType => throw new Exception(s"Invalid transaction type: $txType")
    }
  }

  def bytesToSign(timestamp: Long, senderPubKey: Array[Byte], recipientPubKey: Array[Byte],
                  txNonce: Int, amount: Long, fee: Long): Array[Byte] = {
    Bytes.concat(
      Array(TransactionType.LagonakiTransaction.id.toByte),
      Longs.toByteArray(timestamp),
      senderPubKey,
      recipientPubKey,
      Ints.toByteArray(txNonce),
      Longs.toByteArray(amount),
      Longs.toByteArray(fee))
  }
}