package scorex.transaction.account

trait BalanceSheet {
  val GenerationBalanceConfirmations = 50

  def balance(address: String, height: Option[Int] = None): Long

  def balanceWithConfirmations(address: String, confirmations: Int): Long

  def generationBalance(address: String): Long = balanceWithConfirmations(address, GenerationBalanceConfirmations)

  def generationBalance(account: Account): Long = generationBalance(account.address)
}
