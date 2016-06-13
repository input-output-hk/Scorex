package scorex.transaction

import scorex.block.Block
import scorex.block.Block.BlockId
import scorex.crypto.encode.Base58
import scorex.transaction.box.Proposition
import scorex.transaction.state.MinimalState
import scorex.utils.ScorexLogging

import scala.util.{Failure, Success, Try}

/**
  * Storage interface combining both history(blockchain/blocktree) and state
  */
trait BlockStorage[P <: Proposition] extends ScorexLogging {

  val MaxRollback: Int

  val history: History

  def state: MinimalState[P]

  //Append block to current state
  def appendBlock(block: Block): Try[Unit] = synchronized {
    history.appendBlock(block).map { blocks =>
      blocks foreach { b =>
        state.processBlock(b) match {
          case Failure(e) =>
            log.error("Failed to apply block to state", e)
            removeAfter(block.parentId)
            //TODO ???
            System.exit(1)
          case Success(m) =>
        }
      }
    }
  }

  //Should be used for linear blockchain only
  def removeAfter(id: BlockId): Unit = synchronized {
    history match {
      case h: BlockChain => h.heightOf(id) match {
        case Some(height) =>
          while (!h.lastBlock.id.sameElements(id)) h.discardBlock()
          state.rollbackTo(height)
        case None =>
          log.warn(s"RemoveAfter non-existing block ${Base58.encode(id)}")
      }
      case _ =>
        throw new RuntimeException("Not available for other option than linear blockchain")
    }
  }
}

object BlockStorage {

  sealed trait Direction

  case object Forward extends Direction

  case object Reversed extends Direction
}
