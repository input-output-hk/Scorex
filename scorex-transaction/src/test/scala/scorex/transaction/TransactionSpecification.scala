package scorex.transaction

import org.scalatest.prop.{GeneratorDrivenPropertyChecks, PropertyChecks}
import org.scalatest.{Matchers, PropSpec}
import scorex.account.PrivateKeyAccount
import scorex.crypto.encode.Base58
import scorex.utils.randomBytes

import scala.util.Random

class TransactionSpecification extends PropSpec
with PropertyChecks
with GeneratorDrivenPropertyChecks
with Matchers {


  property("Slice transaction attachment") {
    forAll { (senderSeed: Array[Byte],
              recipientSeed: Array[Byte],
              time: Long,
              amount: Long,
              attachmentRnd: Array[Byte],
              fee: Long) =>
      val attachment:Array[Byte] = attachmentRnd ++ Array.fill(PaymentTransaction.MaxAttachmentSize)(1: Byte)
      val sender = new PrivateKeyAccount(senderSeed)
      val recipient = new PrivateKeyAccount(recipientSeed)

      val tx = PaymentTransaction(sender, recipient, amount, fee, time, attachment)
      val txAfter = PaymentTransaction.parseBytes(tx.bytes).get
      txAfter.attachment.length shouldBe PaymentTransaction.MaxAttachmentSize
    }
  }


  property("transaction signature should be valid in a valid flow") {
    forAll { (senderSeed: Array[Byte],
              recipientSeed: Array[Byte],
              attachment: Array[Byte],
              time: Long,
              amount: Long,
              fee: Long) =>
      val sender = new PrivateKeyAccount(senderSeed)
      val recipient = new PrivateKeyAccount(recipientSeed)

      val tx = PaymentTransaction(sender, recipient, amount, fee, time, attachment)
      tx.signatureValid shouldBe true
    }
  }

  property("wrong transaction signature should be invalid") {
    forAll { (senderSeed: Array[Byte],
              recipientSeed: Array[Byte],
              attachment: Array[Byte],
              time: Long,
              amount: Long,
              fee: Long) =>
      whenever(!(senderSeed sameElements recipientSeed)) {
        val sender = new PrivateKeyAccount(senderSeed)
        val recipient = new PrivateKeyAccount(recipientSeed)

        val sig = PaymentTransaction.generateSignature(sender, recipient, amount, fee, time, attachment)

        new PaymentTransaction(sender, recipient, amount, fee, time, attachment, sig).signatureValid shouldBe true
        new PaymentTransaction(sender, recipient, amount, fee, time, (0: Byte) +: attachment, sig).signatureValid shouldBe false
        new PaymentTransaction(sender, recipient, amount + 1, fee, time, attachment, sig).signatureValid shouldBe false
        new PaymentTransaction(sender, recipient, amount, fee + 1, time, attachment, sig).signatureValid shouldBe false
        new PaymentTransaction(sender, recipient, amount, fee, time + 1, attachment, sig).signatureValid shouldBe false
        new PaymentTransaction(sender, sender, amount, fee, time, attachment, sig).signatureValid shouldBe false
        new PaymentTransaction(recipient, recipient, amount, fee, time, attachment, sig).signatureValid shouldBe false
        new PaymentTransaction(recipient, sender, amount, fee, time, attachment, sig).signatureValid shouldBe false
      }
    }
  }

  property("transaction fields should be constructed in a right way") {
    forAll { (senderSeed: Array[Byte],
              recipientSeed: Array[Byte],
              time: Long,
              attachment: Array[Byte],
              amount: Long,
              fee: Long) =>

      val sender = new PrivateKeyAccount(senderSeed)
      val recipient = new PrivateKeyAccount(recipientSeed)

      val tx = PaymentTransaction(sender, recipient, amount, fee, time, attachment)

      tx.timestamp shouldEqual time
      tx.amount shouldEqual amount
      tx.fee shouldEqual fee
      tx.sender shouldEqual sender
      tx.recipient shouldEqual recipient
    }
  }

  property("bytes()/parse() roundtrip should preserve a transaction") {
    forAll { (senderSeed: Array[Byte],
              recipientSeed: Array[Byte],
              attachment: Array[Byte],
              time: Long,
              amount: Long,
              fee: Long) =>

      val sender = new PrivateKeyAccount(senderSeed)
      val recipient = new PrivateKeyAccount(recipientSeed)
      val tx = PaymentTransaction(sender, recipient, amount, fee, time, attachment)
      val txAfter = PaymentTransaction.parseBytes(tx.bytes).get

      txAfter.getClass.shouldBe(tx.getClass)

      tx.dataLength shouldEqual txAfter.dataLength
      tx.signature shouldEqual txAfter.signature
      tx.sender shouldEqual txAfter.asInstanceOf[PaymentTransaction].sender
      tx.recipient shouldEqual txAfter.recipient
      tx.timestamp shouldEqual txAfter.timestamp
      tx.amount shouldEqual txAfter.amount
      tx.fee shouldEqual txAfter.fee
      txAfter.signatureValid shouldEqual true
    }
  }

  property("PaymentTransaction vector") {
    val bytes = Base58.decode("CzPyJt4mEh3Q8rW3vJG6sdZFwwKDkoYkFDVXhTUUVVPqr3FfBW5UCJHTd3eoVrx1ukbuQiGM3953GB8VJ7yMCePHBMB5spX9D4F6vyBJWva7ZfbG9APje3iKNoBDJLSnPRQCcZUDRYBsSJYL1GGVay25CV9W8aRqjAuyvVVC54TiwExFbaNZ3MrmHaZRr2fS1rjRj38Z").get
    val actualTransaction = PaymentTransaction.parseBytes(bytes).get

    actualTransaction.fee shouldBe 2
    actualTransaction.amount shouldBe 123L
    actualTransaction.sender.address shouldBe "3MTNTc6nrfHrftucagvRyw9PfXffvgJXs6z"
    actualTransaction.recipient.address shouldBe "3MV9Z4owZGKUGuvTStvb7ByeGTmjmXUaQSU"
    actualTransaction.timestamp shouldBe 9223372036854775807L
    Base58.encode(actualTransaction.signature) shouldBe "4ZA1mJgFRSwcCiKQ1Qhb6KKgTBJ1Zt714XWWq2rW6x45ywSfBunZoAuutWduynZBFx3qCb3t8dWnGw1pgtgShuND"
  }

  property("PaymentTransaction should deserialize to LagonakiTransaction") {
    forAll {
      (senderSeed: Array[Byte],
       recipientSeed: Array[Byte],
       attachment: Array[Byte],
       time: Long,
       amount: Long,
       fee: Long) =>
        whenever(attachment.length <= PaymentTransaction.MaxAttachmentSize) {

          val sender = new PrivateKeyAccount(senderSeed)
          val recipient = new PrivateKeyAccount(recipientSeed)
          val tx = PaymentTransaction(sender, recipient, amount, fee, time, attachment)
          val txAfter = LagonakiTransaction.parseBytes(tx.bytes).get

          txAfter.getClass.shouldBe(tx.getClass)

          tx.dataLength shouldEqual txAfter.dataLength
          tx.signature shouldEqual txAfter.signature
          tx.sender shouldEqual txAfter.asInstanceOf[PaymentTransaction].sender
          tx.recipient shouldEqual txAfter.recipient
          tx.timestamp shouldEqual txAfter.timestamp
          tx.amount shouldEqual txAfter.amount
          tx.fee shouldEqual txAfter.fee
          tx.attachment shouldEqual txAfter.asInstanceOf[PaymentTransaction].attachment
          txAfter.signatureValid shouldEqual true
        }
    }
  }
}