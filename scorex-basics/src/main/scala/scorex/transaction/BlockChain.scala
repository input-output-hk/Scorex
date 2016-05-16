package scorex.transaction

import scorex.block.Block
import scorex.block.Block.BlockId
import scorex.utils.ScorexLogging

trait BlockChain[TX <: Transaction[_]] extends History[TX] with ScorexLogging {

  def blockAt(height: Int): Option[Block[TX]]

  def genesisBlock: Option[Block[TX]] = blockAt(1)

  override def parent(block: Block[TX], back: Int = 1): Option[Block[TX]] = {
    require(back > 0)
    heightOf(block.referenceField.value).flatMap(referenceHeight => blockAt(referenceHeight - back + 1))
  }

  private[transaction] def discardBlock(): BlockChain[TX]

  override def lastBlocks(howMany: Int): Seq[Block[TX]] =
    (Math.max(1, height() - howMany + 1) to height()).flatMap(blockAt).reverse

  def lookForward(parentSignature: BlockId, howMany: Int): Seq[BlockId] =
    heightOf(parentSignature).map { h =>
      (h + 1).to(Math.min(height(), h + howMany: Int)).flatMap(blockAt).map(_.uniqueId)
    }.getOrElse(Seq())

  def children(block: Block[TX]): Seq[Block[TX]]

  override lazy val genesis: Block[TX] = blockAt(1).get
}