package scorex.transaction

import scorex.block.{Block, BlockProcessingModule}
import scorex.transaction.account.{AccountTransactionsHistory, BalanceSheet}
import scorex.transaction.state.MinimalState

trait TransactionModule[TBD, TX <: Transaction] extends BlockProcessingModule[TBD] {

  val blockStorage: BlockStorage[TX]

  def isValid(block: Block[TX]): Boolean

  def transactions(block: Block[TX]): Seq[TX]

  def packUnconfirmed(): TBD

  def clearFromUnconfirmed(data: TBD): Unit

  def onNewOffchainTransaction(transaction: TX): Unit

  def toSign(block: Block[TX]): Array[Byte]

  lazy val balancesSupport: Boolean = blockStorage.state match {
    case _: MinimalState[AccountTransaction[_]] with BalanceSheet => true
    case _ => false
  }

  lazy val accountWatchingSupport: Boolean = blockStorage.state match {
    case _: MinimalState[AccountTransaction[_]] with AccountTransactionsHistory[_] => true
    case _ => false
  }
}