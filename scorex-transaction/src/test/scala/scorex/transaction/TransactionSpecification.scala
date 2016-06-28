package scorex.transaction

import org.scalatest.prop.{GeneratorDrivenPropertyChecks, PropertyChecks}
import org.scalatest.{Matchers, PropSpec}
import scorex.transaction.state.SecretGenerator25519

class TransactionSpecification extends PropSpec
  with PropertyChecks
  with GeneratorDrivenPropertyChecks
  with Matchers {

  property("transaction signature should be valid in a valid flow") {
    forAll { (senderSeed: Array[Byte],
              recipientSeed: Array[Byte],
              nonce: Int,
              time: Long,
              amount: Long,
              fee: Long) =>
      val sender = SecretGenerator25519.generateKeys(senderSeed)
      val recipient = SecretGenerator25519.generateKeys(recipientSeed).publicCommitment

      val tx = LagonakiTransaction(sender, recipient, nonce, amount, fee, time)
      sender.verify(tx.messageToSign, tx.signature) should be(true)
    }
  }

  property("wrong transaction signature should be invalid") {
    forAll { (senderSeed: Array[Byte],
              recipientSeed: Array[Byte],
              nonce: Int,
              time: Long,
              amount: Long,
              fee: Long) =>
      val sender = SecretGenerator25519.generateKeys(senderSeed)
      val recipient = SecretGenerator25519.generateKeys(recipientSeed).publicCommitment

      val sig = LagonakiTransaction(sender, recipient, nonce, amount, fee, time).signature

      sender.verify(LagonakiTransaction(sender, recipient, nonce + 1, amount, fee, time).messageToSign, sig) should be(false)
      sender.verify(LagonakiTransaction(sender, recipient, nonce, amount + 1, fee, time).messageToSign, sig) should be(false)
      sender.verify(LagonakiTransaction(sender, recipient, nonce, amount, fee + 1, time).messageToSign, sig) should be(false)
      sender.verify(LagonakiTransaction(sender, recipient, nonce, amount, fee, time + 1).messageToSign, sig) should be(false)
    }
  }

  property("transaction fields should be constructed in a right way") {
    forAll { (senderSeed: Array[Byte],
              recipientSeed: Array[Byte],
              nonce: Int,
              time: Long,
              amount: Long,
              fee: Long) =>

      val sender = SecretGenerator25519.generateKeys(senderSeed)
      val recipient = SecretGenerator25519.generateKeys(recipientSeed).publicCommitment

      val tx = LagonakiTransaction(sender, recipient, nonce, amount, fee, time)

      tx.txnonce shouldEqual nonce
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
              nonce: Int,
              time: Long,
              amount: Long,
              fee: Long) =>

      val sender = SecretGenerator25519.generateKeys(senderSeed)
      val recipient = SecretGenerator25519.generateKeys(recipientSeed).publicCommitment

      val tx: LagonakiTransaction = LagonakiTransaction(sender, recipient, nonce, amount, fee, time)
      val txAfter = LagonakiTransaction.parseBytes(tx.bytes).get

      txAfter.getClass.shouldBe(tx.getClass)

      tx.signature shouldEqual txAfter.signature
      tx.sender shouldEqual txAfter.sender
      tx.recipient shouldEqual txAfter.recipient
      tx.timestamp shouldEqual txAfter.timestamp
      tx.amount shouldEqual txAfter.amount
      tx.fee shouldEqual txAfter.fee

      sender.verify(tx.messageToSign, txAfter.signature) should be(true)
      sender.verify(txAfter.messageToSign, txAfter.signature) should be(true)
      sender.verify(txAfter.messageToSign, tx.signature) should be(true)
    }
  }

  ignore("GenesisTransaction Signature should be the same") {

  }

  ignore("GenesisTransaction parse from Bytes should work fine") {

  }
}