package scorex.transaction.state

import scorex.block.Block
import scorex.transaction.Transaction
import scorex.transaction.box.{Box, Proposition}

import scala.util.Try

trait BoxMinimalState[L <: Proposition] extends MinimalState[Box[L]] {

  /**
    * The only question minimal box state could answer is "whether a box is closed?",
    * as it stores closed boxes only
    * Please note the answer is "no" even if a box never existed
    */
  def boxIsClosed(boxId: Box[L]#Id): Boolean

  override private[transaction] def processBlock(block: Block): Try[BoxMinimalState[L]]

  override def isValid(tx: Transaction): Boolean

  override def areValid(txs: Seq[Transaction], height: Option[Int] = None): Boolean

  override def validate(txs: Seq[Transaction], height: Option[Int] = None): Seq[Transaction]
}
