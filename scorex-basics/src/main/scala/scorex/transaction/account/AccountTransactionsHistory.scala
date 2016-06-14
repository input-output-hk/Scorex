package scorex.transaction.account

import scorex.transaction.Transaction
import scorex.transaction.box.Proposition

trait AccountTransactionsHistory[P <: Proposition] {
  def accountTransactions(id: P): Array[Transaction[P]]
}