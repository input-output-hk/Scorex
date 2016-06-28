package scorex.transaction.state

import scorex.block.Block
import scorex.transaction._
import scorex.transaction.box.{Box, PublicKey25519Proposition}
import scorex.utils.ScorexLogging

import scala.util.Try


/** Store current balances only, and balances changes within effective balance depth.
  * Store transactions for selected accounts only.
  * If no filename provided, blockchain lives in RAM (intended for tests only).
  *
  * Use apply method of PersistentLagonakiState object to create new instance
  */
trait PersistentLagonakiState extends LagonakiState with ScorexLogging {
  val dirNameOpt: Option[String]

  override def accountTransactions(id: PublicKey25519Proposition): Array[LagonakiTransaction] = ???

  override val version: Int = ???

  override def closedBox(boxId: Array[Byte]): Option[Box[PublicKey25519Proposition]] = ???

  override def rollbackTo(height: Int): Try[Unit] = ???

  override def processBlock(block: Block[PublicKey25519Proposition, _, _]): Try[Unit] = ???

  override def balance(id: PublicKey25519Proposition, height: Option[Int]): Long = ???

  override def balanceWithConfirmations(id: PublicKey25519Proposition, confirmations: Int): Long = ???
}