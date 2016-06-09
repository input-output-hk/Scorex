package scorex.consensus

import scorex.block.Block
import scorex.transaction.AccountTransaction
import scorex.transaction.account.Account

/**
  * Data and functions related to a consensus algo
  */

trait LagonakiConsensusModule[ConsensusBlockData <: LagonakiConsensusBlockData, TX <: AccountTransaction] extends ConsensusModule[ConsensusBlockData, Account, TX] {

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
}
