package scorex.transaction

import scorex.block.{Block, BlockProcessingModule}
import scorex.transaction.account.BalanceSheet

trait TransactionModule[TBD] extends BlockProcessingModule[TBD] {

  val blockStorage: BlockStorage

  def isValid(block: Block): Boolean

  def transactions(block: Block): Seq[Transaction]

  def packUnconfirmed(): TBD

  def clearFromUnconfirmed(data: TBD): Unit

  def onNewOffchainTransaction(transaction: Transaction): Unit

  lazy val balancesSupport: Boolean = blockStorage.state match {
    case _: AccountMinimalState with BalanceSheet => true
    case _ => false
  }

  lazy val accountWatchingSupport: Boolean = blockStorage.state match {
    case _: AccountMinimalState with AccountTransactionsHistory => true
    case _ => false
  }
}