package scorex.transaction.state

import scorex.block.Block
import scorex.transaction.account.Account
import scorex.transaction.box.{Box, Proposition}
import scorex.transaction.{BoxTransaction, AccountTransaction, Transaction}
import scala.util.Try

/**
  * Abstract functional interface of state which is a result of a sequential blocks applying
  */

sealed trait MinimalState[SE <: StateElement, TX <: Transaction[SE]] {
  val version: Int

  private[transaction] def processBlock(block: Block[SE, TX]): Try[MinimalState[SE, TX]]

  def isValid(tx: TX): Boolean

  def areValid(txs: Seq[TX], height: Option[Int] = None): Boolean

  def validate(txs: Seq[TX], height: Option[Int] = None): Seq[TX]

  private[transaction] def rollbackTo(height: Int): Try[MinimalState[SE, TX]]
}


trait AccountMinimalState[ATX <: AccountTransaction] extends MinimalState[Account, ATX] {

  override def validate(txs: Seq[ATX], height: Option[Int] = None): Seq[ATX]

  override def isValid(tx: ATX): Boolean = areValid(Seq(tx))

  override def areValid(txs: Seq[ATX], height: Option[Int] = None): Boolean = validate(txs, height).size == txs.size

  def included(signature: Array[Byte], heightOpt: Option[Int]): Option[Int]

  def included(transaction: ATX, heightOpt: Option[Int] = None): Option[Int] =
    included(transaction.proof.bytes, heightOpt)
}



trait BoxMinimalState[Prop <: Proposition] extends MinimalState[Box[Prop], BoxTransaction[Prop]] {

  /**
    * The only question minimal box state could answer is "whether a box is closed?",
    * as it stores closed boxes only
    * Please note the answer is "no" even if a box never existed
    */
  def boxIsClosed(boxId: Array[Byte]): Boolean

  override private[transaction] def processBlock(block: Block[Box[Prop], BoxTransaction[Prop]]): Try[BoxMinimalState[Prop]]

  override def isValid(tx: BoxTransaction[Prop]): Boolean

  override def areValid(txs: Seq[BoxTransaction[Prop]], height: Option[Int] = None): Boolean

  override def validate(txs: Seq[BoxTransaction[Prop]], height: Option[Int] = None): Seq[BoxTransaction[Prop]]
}