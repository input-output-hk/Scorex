package scorex.transaction.account

import scorex.transaction.Transaction

trait AccountTransaction extends Transaction {
  val recipient: Account
  val signature: Array[Byte]

  override lazy val serializedProof: Array[Byte] = signature
}
