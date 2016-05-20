package scorex.transaction.state

import scorex.block.Block
import scorex.transaction.box.{Box, Proposition}
import scorex.transaction.{BoxTransaction, AccountTransaction, Transaction}
import scala.util.Try

/**
  * Abstract functional interface of state which is a result of a sequential blocks applying
  */

sealed trait MinimalState[TX <: Transaction[_]] {
  val version: Int

  private[transaction] def processBlock(block: Block[TX]): Try[MinimalState[TX]]

  def isValid(tx: TX): Boolean

  def areValid(txs: Seq[TX], height: Option[Int] = None): Boolean

  def validate(txs: Seq[TX], height: Option[Int] = None): Seq[TX]

  private[transaction] def rollbackTo(height: Int): Try[MinimalState[TX]]
}


trait AccountMinimalState[ATX <: AccountTransaction] extends MinimalState[ATX] {

  override def validate(txs: Seq[ATX], height: Option[Int] = None): Seq[ATX]

  override def isValid(tx: ATX): Boolean = areValid(Seq(tx))

  override def areValid(txs: Seq[ATX], height: Option[Int] = None): Boolean = validate(txs, height).size == txs.size

  def included(signature: Array[Byte], heightOpt: Option[Int]): Option[Int]

  def included(transaction: ATX, heightOpt: Option[Int] = None): Option[Int] =
    included(transaction.proof.bytes, heightOpt)
}



trait BoxMinimalState[Prop <: Proposition] extends MinimalState[BoxTransaction[Prop]] {

  /**
    * The only question minimal box state could answer is "whether a box is closed?",
    * as it stores closed boxes only
    * Please note the answer is "no" even if a box never existed
    */
  def boxIsClosed(boxId: Box[Prop]#Id): Boolean

  override private[transaction] def processBlock(block: Block[BoxTransaction[Prop]]): Try[BoxMinimalState[Prop]]

  override def isValid(tx: BoxTransaction[Prop]): Boolean

  override def areValid(txs: Seq[BoxTransaction[Prop]], height: Option[Int] = None): Boolean

  override def validate(txs: Seq[BoxTransaction[Prop]], height: Option[Int] = None): Seq[BoxTransaction[Prop]]
}