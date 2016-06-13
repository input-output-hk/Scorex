package scorex.transaction.state.database

import scorex.transaction.Transaction

abstract class UnconfirmedTransactionsDatabase[TX <: Transaction[_]] {

  def putIfNew(tx: TX): Boolean

  def all(): Seq[TX]

  def getBySignature(signature: Array[Byte]): Option[TX]

  def remove(tx: TX)
}