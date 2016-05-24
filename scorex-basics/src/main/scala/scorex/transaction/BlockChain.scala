package scorex.transaction

import scorex.block.Block
import scorex.block.Block.BlockId
import scorex.transaction.state.StateElement
import scorex.utils.ScorexLogging

trait BlockChain[SE <: StateElement, TX <: Transaction[SE]] extends History[SE, TX] with ScorexLogging {

  def blockAt(height: Int): Option[Block[SE, TX]]

  def genesisBlock: Option[Block[SE, TX]] = blockAt(1)

  override def parent(block: Block[SE, TX], back: Int = 1): Option[Block[SE, TX]] = {
    require(back > 0)
    heightOf(block.referenceField.value).flatMap(referenceHeight => blockAt(referenceHeight - back + 1))
  }

  private[transaction] def discardBlock(): BlockChain[SE, TX]

  override def lastBlocks(howMany: Int): Seq[Block[SE, TX]] =
    (Math.max(1, height() - howMany + 1) to height()).flatMap(blockAt).reverse

  def lookForward(parentSignature: BlockId, howMany: Int): Seq[BlockId] =
    heightOf(parentSignature).map { h =>
      (h + 1).to(Math.min(height(), h + howMany: Int)).flatMap(blockAt).map(_.uniqueId)
    }.getOrElse(Seq())

  def children(block: Block[SE, TX]): Seq[Block[SE, TX]]

  override lazy val genesis: Block[SE, TX] = blockAt(1).get
}