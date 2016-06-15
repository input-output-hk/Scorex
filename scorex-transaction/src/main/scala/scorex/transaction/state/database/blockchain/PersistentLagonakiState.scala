package scorex.transaction.state.database.blockchain

import scorex.block.Block
import scorex.transaction._
import scorex.transaction.box.{Box, Proposition, PublicKey25519Proposition}
import scorex.transaction.state.{LagonakiState, MinimalState}
import scorex.utils.ScorexLogging

import scala.util.Try


/** Store current balances only, and balances changes within effective balance depth.
  * Store transactions for selected accounts only.
  * If no filename provided, blockchain lives in RAM (intended for tests only).
  *
  * Use apply method of PersistentLagonakiState object to create new instance
  */
class PersistentLagonakiState(dirNameOpt: Option[String]) extends LagonakiState with ScorexLogging {
  override def accountTransactions(id: PublicKey25519Proposition): Array[Transaction[PublicKey25519Proposition]] = ???

  override val version: Int = _

  override def closedBox(boxId: Array[Byte]): Option[Box[PublicKey25519Proposition]] = ???

  override private[transaction] def rollbackTo(height: Int): Try[MinimalState[PublicKey25519Proposition]] = ???

  override private[transaction] def processBlock(block: Block): Try[MinimalState[PublicKey25519Proposition]] = ???

  override def balance(id: Proposition, height: Option[Int]): Long = ???

  override def balanceWithConfirmations(id: Proposition, confirmations: Int): Long = ???
}