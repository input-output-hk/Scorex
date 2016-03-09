package scorex.transaction.state.database

import scorex.transaction.account.AccountTransaction

trait UnconfirmedTransactionsDatabase {
  def putIfNew(tx: AccountTransaction): Boolean

  def all(): Seq[AccountTransaction]

  def getBySignature(signature: Array[Byte]): Option[AccountTransaction]

  def remove(tx: AccountTransaction)
}