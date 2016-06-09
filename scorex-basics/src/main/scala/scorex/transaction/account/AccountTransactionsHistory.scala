package scorex.transaction.account

import scorex.transaction.Transaction
import scorex.transaction.box.Proposition

trait AccountTransactionsHistory[P <: Proposition] {
  def accountTransactions(id: P): Array[Transaction]

  /*def accountTransactions(address: String): Array[_ <: AccountTransaction] = {
    Account.isValidAddress(address) match {
      case false => Array()
      case true =>
        val acc = new Account(address)
        accountTransactions(acc)
    }
  }*/
}