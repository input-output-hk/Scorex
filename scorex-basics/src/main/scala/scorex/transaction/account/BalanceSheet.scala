package scorex.transaction.account

import scorex.transaction.box.Proposition


trait BalanceSheet {
  val GenerationBalanceConfirmations = 50

  def balance(id: Proposition, height: Option[Int] = None): Long

  def balanceWithConfirmations(id: Proposition, confirmations: Int): Long

  def generationBalance(id: Proposition): Long = balanceWithConfirmations(id, GenerationBalanceConfirmations)
}
