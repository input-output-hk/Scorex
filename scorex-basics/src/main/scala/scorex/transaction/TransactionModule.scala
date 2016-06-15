package scorex.transaction

import scorex.block.{Block, BlockProcessingModule}
import scorex.settings.Settings
import scorex.transaction.account.{AccountTransactionsHistory, BalanceSheet}
import scorex.transaction.box.{AddressableProposition, Proposition}
import scorex.transaction.proof.Proof
import scorex.transaction.state.{SecretHolderGenerator, SecretHolder, MinimalState}
import scorex.wallet.Wallet

//todo: add pool
trait TransactionModule {
  type TBD
  val builder: BlockProcessingModule[TBD]

  type P <: Proposition
  type TX <: Transaction[P]
  type PR <: Proof[P]
  type SH <: SecretHolder[_ <: P with AddressableProposition, PR]

  val settings: Settings

  val generator: SecretHolderGenerator[SH]

  //wallet
  private val walletFileOpt = settings.walletDirOpt.map(walletDir => new java.io.File(walletDir, "wallet.s.dat"))
  val wallet = new Wallet(walletFileOpt, settings.walletPassword, settings.walletSeed, generator)

  val blockStorage: BlockStorage[P]

  def isValid(block: Block): Boolean

  def transactions(block: Block): Seq[TX]

  def packUnconfirmed(): TBD

  def clearFromUnconfirmed(data: TBD): Unit
  def onNewOffchainTransaction(transaction: TX): Unit

  def toSign(block: Block): Array[Byte]

  lazy val balancesSupport: Boolean = blockStorage.state match {
    case _: MinimalState[_] with BalanceSheet => true
    case _ => false
  }

  lazy val accountWatchingSupport: Boolean = blockStorage.state match {
    case _: MinimalState[_] with AccountTransactionsHistory[_] => true
    case _ => false
  }

  def stop() = {
    wallet.close()
  }
}