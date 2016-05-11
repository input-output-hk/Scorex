package scorex.transaction.state

import scorex.block.Block
import scorex.transaction.Transaction

import scala.util.Try

/**
  * Abstract functional interface of state which is a result of a sequential blocks applying
  */

trait MinimalState[ELEM <: StateElement] {
  val version: Int

  private[transaction] def processBlock(block: Block): Try[MinimalState[ELEM]]

  def isValid(tx: Transaction): Boolean

  def areValid(txs: Seq[Transaction], height: Option[Int] = None): Boolean

  def validate(txs: Seq[Transaction], height: Option[Int] = None): Seq[Transaction]

  private[transaction] def rollbackTo(height: Int): Try[MinimalState[ELEM]]
}
