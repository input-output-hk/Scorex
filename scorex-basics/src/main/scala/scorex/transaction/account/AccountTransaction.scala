package scorex.transaction.account

import scorex.transaction.{Signature, Proof, Transaction}

trait AccountTransaction extends Transaction {
  val recipient: Account
  val signature: Array[Byte]

  override lazy val proof: Proof = Signature(signature)
}
