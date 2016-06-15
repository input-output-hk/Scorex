package scorex.consensus

import scorex.block.Block
import scorex.transaction.{TransactionModule, AccountTransaction}
import scorex.transaction.account.Account
import scorex.transaction.state.database.blockchain.StoredBlockchain

/**
  * Data and functions related to a consensus algo
  */

trait LagonakiConsensusModule[TM <: TransactionModule] extends ConsensusModule[TransactionModule] {

  type ConsensusBlockData <: LagonakiConsensusBlockData
  /**
    * In Lagonaki, for both consensus modules, there's only one block generator
    * @param block - block to extract fees distribution from
    * @return
    */
  override def feesDistribution(block: Block[Account, TX]): Map[Account, Long] = {
    val cm = block.consensusModule
    val forger = cm.producers(block).ensuring(_.size == 1).head
    val fee = block.transactions.map(_.fee).sum
    Map(forger -> fee)
  }

  override def producers(block: Block[Account, TX]): Seq[Account]

  override val MaxRollback: Int = settings.MaxRollback

  override val history: History = settings.history match {
    case s: String if s.equalsIgnoreCase("blockchain") =>
      new StoredBlockchain[SimpleTransactionModule](settings.dataDirOpt)(consensusModule, instance)
    case s =>
      log.error(s"Unknown history storage: $s. Use StoredBlockchain...")
      new StoredBlockchain(settings.dataDirOpt)(consensusModule, instance)
  }
}
