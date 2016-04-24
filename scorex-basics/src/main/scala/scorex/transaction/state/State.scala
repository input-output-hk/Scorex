package scorex.transaction.state

import scorex.block.Block
import scorex.transaction.Transaction
import scorex.transaction.account.Account
import scorex.transaction.box.{Lock, Box}

import scala.util.Try

/**
  * Abstract functional interface of state which is a result of a sequential blocks applying
  */

trait MinimalState[ELEM <: StateElement] {
  val version: Int

  private[transaction] def processBlock(block: Block): Try[MinimalState[ELEM]]

  def isValid(tx: Transaction): Boolean = isValid(Seq(tx))

  def isValid(txs: Seq[Transaction], height: Option[Int] = None): Boolean
  = validate(txs, height).size == txs.size

  def validate(txs: Seq[Transaction], height: Option[Int] = None): Seq[Transaction]

  private[transaction] def rollbackTo(height: Int): Try[MinimalState[ELEM]]
}

trait BoxMinimalState[L <: Lock] extends MinimalState[Box[L]]

//todo: implement
//we assume only boxed state could be Merkelized
trait MerkelizedBoxMinimalState[L <: Lock] extends BoxMinimalState[L]

trait AccountMinimalState extends MinimalState[Account] {
  def included(signature: Array[Byte], heightOpt: Option[Int]): Option[Int]

  def included(transaction: Transaction, heightOpt: Option[Int] = None): Option[Int] =
    included(transaction.proof.bytes, heightOpt)
}