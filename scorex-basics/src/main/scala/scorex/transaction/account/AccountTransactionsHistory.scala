package scorex.transaction.account

import scorex.transaction.AccountTransaction

trait AccountTransactionsHistory {
  def accountTransactions(address: String): Array[_ <: AccountTransaction] = {
    Account.isValidAddress(address) match {
      case false => Array()
      case true =>
        val acc = new Account(address)
        accountTransactions(acc)
    }
  }

  def accountTransactions(account: Account): Array[_ <: AccountTransaction]
}
