package scorex.transaction

import scorex.transaction.account.BalanceSheet

trait LagonakiState extends AccountMinimalState with BalanceSheet with AccountTransactionsHistory
