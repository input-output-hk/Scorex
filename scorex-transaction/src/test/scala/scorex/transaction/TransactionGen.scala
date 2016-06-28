package scorex.transaction

import org.scalacheck.{Arbitrary, Gen}
import scorex.transaction.state.SecretGenerator25519

trait TransactionGen {
  val paymentGenerator: Gen[LagonakiTransaction] = for {
    senderSeed: Array[Byte] <- Arbitrary.arbitrary[Array[Byte]]
    rcpSeed: Array[Byte] <- Arbitrary.arbitrary[Array[Byte]]
    nonce: Int <- Arbitrary.arbitrary[Int]
    amount: Long <- Arbitrary.arbitrary[Long]
    fee: Long <- Arbitrary.arbitrary[Long]
    timestamp: Long <- Arbitrary.arbitrary[Long]
  } yield
    LagonakiTransaction(SecretGenerator25519.generateKeys(senderSeed),
      SecretGenerator25519.generateKeys(rcpSeed).publicCommitment,
      nonce,
      amount,
      fee,
      timestamp)
}
