package scorex.transaction

import scorex.block.{Block, BlockProcessingModule}
import scorex.transaction.account.{Account, AccountTransactionsHistory, BalanceSheet}
import scorex.transaction.state.{MinimalState, StateElement, AccountMinimalState}

trait TransactionModule[TBD, TX <: Transaction[_]] extends BlockProcessingModule[TBD] {

  val blockStorage: BlockStorage[TX]

  def isValid(block: Block[TX]): Boolean

  def transactions(block: Block[TX]): Seq[TX]

  def packUnconfirmed(): TBD

  def clearFromUnconfirmed(data: TBD): Unit

  def onNewOffchainTransaction(transaction: TX): Unit

  lazy val balancesSupport: Boolean = blockStorage.state match {
    case _: MinimalState[AccountTransaction] with BalanceSheet => true
    case _ => false
  }

  lazy val accountWatchingSupport: Boolean = blockStorage.state match {
    case _: MinimalState[AccountTransaction] with AccountTransactionsHistory => true
    case _ => false
  }
}