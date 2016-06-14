package scorex.transaction.state

import scorex.block.Block
import scorex.transaction.Transaction
import scorex.transaction.box.{Box, Proposition}
import scala.util.Try

/**
  * Abstract functional interface of state which is a result of a sequential blocks applying
  */

trait MinimalState[P <: Proposition] {
  val version: Int

  def isValid(tx: Transaction[P]): Boolean = tx.validate(this).isSuccess

  def areValid(txs: Seq[Transaction[P]]): Boolean = txs.forall(isValid)

  def filterValid(txs: Seq[Transaction[P]]): Seq[Transaction[P]] = txs.filter(isValid)

  private[transaction] def processBlock(block: Block): Try[MinimalState[P]]

  private[transaction] def rollbackTo(height: Int): Try[MinimalState[P]]

  def closedBox(boxId: Array[Byte]): Option[Box[P]]
}