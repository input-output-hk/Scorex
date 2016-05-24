package scorex.transaction

import scorex.block.{Block, BlockProcessingModule}
import scorex.transaction.account.{Account, AccountTransactionsHistory, BalanceSheet}
import scorex.transaction.state.{MinimalState, StateElement}

trait TransactionModule[TBD, SE <: StateElement, TX <: Transaction[SE]] extends BlockProcessingModule[TBD] {

  val blockStorage: BlockStorage[SE, TX]

  def isValid(block: Block[SE, TX]): Boolean

  def transactions(block: Block[SE, TX]): Seq[TX]

  def packUnconfirmed(): TBD

  def clearFromUnconfirmed(data: TBD): Unit

  def onNewOffchainTransaction(transaction: TX): Unit

  lazy val balancesSupport: Boolean = blockStorage.state match {
    case _: MinimalState[Account, AccountTransaction] with BalanceSheet => true
    case _ => false
  }

  lazy val accountWatchingSupport: Boolean = blockStorage.state match {
    case _: MinimalState[Account, AccountTransaction] with AccountTransactionsHistory => true
    case _ => false
  }
}