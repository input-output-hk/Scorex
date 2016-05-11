package scorex.transaction.state

import scorex.transaction.Transaction
import scorex.transaction.account.Account

trait AccountMinimalState extends MinimalState[Account] {

  override def isValid(tx: Transaction): Boolean = areValid(Seq(tx))

  override def areValid(txs: Seq[Transaction], height: Option[Int] = None): Boolean = validate(txs, height).size == txs.size

  def included(signature: Array[Byte], heightOpt: Option[Int]): Option[Int]

  def included(transaction: Transaction, heightOpt: Option[Int] = None): Option[Int] =
    included(transaction.proof.bytes, heightOpt)
}