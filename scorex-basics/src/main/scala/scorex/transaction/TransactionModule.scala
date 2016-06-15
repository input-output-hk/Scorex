package scorex.transaction

import scorex.block.{Block, BlockProcessingModule}
import scorex.settings.Settings
import scorex.transaction.account.{AccountTransactionsHistory, BalanceSheet}
import scorex.transaction.box.{AddressableProposition, Proposition}
import scorex.transaction.proof.Proof
import scorex.transaction.state.{MinimalState, SecretHolder, SecretHolderGenerator}
import scorex.utils.ScorexLogging
import scorex.wallet.Wallet

//todo: add pool
trait TransactionModule extends ScorexLogging {
  type TBD
  val builder: BlockProcessingModule[TBD]

  type P <: Proposition
  type AP <: AddressableProposition

  type TX <: Transaction[P]
  type PR <: Proof[P]
  type SH <: SecretHolder[_ <: P with AddressableProposition, PR]

  val settings: Settings

  val generator: SecretHolderGenerator[SH]

  //wallet
  private val walletFileOpt = settings.walletDirOpt.map(walletDir => new java.io.File(walletDir, "wallet.s.dat"))
  val wallet = new Wallet(walletFileOpt, settings.walletPassword, settings.walletSeed, generator)

  def isValid(block: Block): Boolean

  def transactions(block: Block): Seq[TX]

  def packUnconfirmed(): TBD

  def clearFromUnconfirmed(data: TBD): Unit

  def onNewOffchainTransaction(transaction: TX): Unit

  def toSign(block: Block): Array[Byte]

  def state: MinimalState[P]

  lazy val balancesSupport: Boolean = state match {
    case _: MinimalState[_] with BalanceSheet[_] => true
    case _ => false
  }

  lazy val accountWatchingSupport: Boolean = state match {
    case _: MinimalState[_] with AccountTransactionsHistory[_] => true
    case _ => false
  }

  def stop(): Unit = {
    wallet.close()
  }
}