package scorex.transaction.state

import scorex.transaction.LagonakiTransaction
import scorex.transaction.account.{AccountTransactionsHistory, BalanceSheet}

trait LagonakiState extends AccountMinimalState[LagonakiTransaction] with BalanceSheet with AccountTransactionsHistory[_]
