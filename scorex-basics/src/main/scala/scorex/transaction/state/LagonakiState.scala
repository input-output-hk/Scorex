package scorex.transaction.state

import scorex.transaction.account.{AccountTransactionsHistory, BalanceSheet}

trait LagonakiState extends AccountMinimalState with BalanceSheet with AccountTransactionsHistory
