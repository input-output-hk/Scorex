package scorex.transaction

import com.google.common.primitives.Ints
import play.api.libs.json.Json
import scorex.crypto.encode.Base58
import scorex.serialization.{BytesParseable, BytesSerializable}
import scorex.transaction.LagonakiTransaction.TransactionType
import scorex.transaction.account.PublicKey25519NoncedBox
import scorex.transaction.box.{BoxUnlocker, PublicKey25519Proposition, PublicKeyProposition}
import scorex.transaction.proof.{Proof, Signature25519}

import scala.util.{Failure, Try}


abstract class LagonakiTransaction(val transactionType: TransactionType.Value,
                                   val sender: PublicKey25519NoncedBox,
                                   val recipient: PublicKey25519NoncedBox,
                                   val txnonce: Int,
                                   val amount: Long,
                                   override val fee: Long,
                                   override val timestamp: Long,
                                   val signature: Array[Byte])
  extends Transaction[PublicKey25519Proposition] with BytesSerializable {

  val dataLength: Int

  override val unlockers: Traversable[BoxUnlocker[PublicKey25519Proposition]] = Seq(
    new BoxUnlocker[PublicKey25519Proposition] {
      override val closedBoxId: Array[Byte] = sender.id
      override val boxKey: Proof[PublicKey25519Proposition] = Signature25519(signature)
      override lazy val bytes = closedBoxId ++ signature
    }
  )

  override val newBoxes: Traversable[PublicKey25519NoncedBox] = Seq(
    PublicKey25519NoncedBox(sender.pubKey, sender.nonce + 1, sender.value - amount - fee),
    PublicKey25519NoncedBox(recipient.pubKey, recipient.nonce, recipient.value + amount)
  )

  override def equals(other: Any): Boolean = other match {
    case tx: LagonakiTransaction => signature.sameElements(tx.signature)
    case _ => false
  }

  override def hashCode(): Int = Ints.fromByteArray(signature)

  protected def jsonBase() =
    Json.obj("type" -> transactionType.id,
      "fee" -> fee,
      "timestamp" -> timestamp,
      "signature" -> Base58.encode(this.signature)
    )
}

object LagonakiTransaction extends BytesParseable[LagonakiTransaction] {

  val RecipientLength = PublicKeyProposition.AddressLength
  val TypeLength = 1
  val TimestampLength = 8
  val AmountLength = 8

  object ValidationResult extends Enumeration {
    type ValidationResult = Value

    val ValidateOke = Value(1)
    val InvalidAddress = Value(2)
    val NegativeAmount = Value(3)
    val NegativeFee = Value(4)
    val NoBalance = Value(5)
  }

  object TransactionType extends Enumeration {
    val GenesisTransaction = Value(1)
    val PaymentTransaction = Value(2)
  }

  def parseBytes(data: Array[Byte]): Try[LagonakiTransaction] =
    data.head match {
      case txType: Byte if txType == TransactionType.GenesisTransaction.id =>
        GenesisTransaction.parseBytes(data.tail)

      case txType: Byte if txType == TransactionType.PaymentTransaction.id =>
        PaymentTransaction.parseBytes(data.tail)

      case txType => Failure(new Exception(s"Invalid transaction type: $txType"))
    }
}