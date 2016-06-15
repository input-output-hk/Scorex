package scorex.consensus

import play.api.libs.json.Json
import scorex.block.{BlockProcessingModule, BlockIdField, Block}
import scorex.crypto.encode.Base58
import scorex.transaction.box.Proposition
import scorex.transaction.TransactionModule
import scorex.utils.ScorexLogging
import shapeless.{Nat, Sized}
import scala.concurrent.Future
import scala.util.{Success, Failure, Try}


trait BasicConsensusBlockData {
  type IdSize <: Nat

  val id: Sized[Array[Byte], IdSize]
  val parentId: Sized[Array[Byte], IdSize]
  val producers: Traversable[Proposition]

  private lazy val blockIdField = BlockIdField("id", id)
  private lazy val parentIdField = BlockIdField("parent", parentId)

  lazy val fields = Seq(blockIdField, parentIdField)

  lazy val json = Json.arr(fields.map(_.json))
  lazy val bytes = fields.view.map(_.value).reduce(_ ++ _)
}

//todo: finish
trait SingleProducerConsensusBlockData extends BasicConsensusBlockData {
  //val producerField: ProducerBlockField
  //val signatureField: SignatureBlockField
  //lazy val history = transactionModule.blockStorage.history.contains(referenceField.value)
  //lazy val sigValid = producer.verify(messageToSign, signature, nonce)
}


trait ConsensusModule[TM <: TransactionModule] extends ScorexLogging {

  val transactionModule: TM

  type ConsensusBlockData <: BasicConsensusBlockData
  val builder: BlockProcessingModule[ConsensusBlockData]

  type Prop = TM#P
  type BlockId = Array[Byte]

  val BlockIdLength: Int

  def isValid[TransactionalBlockData](block: Block): Boolean

  /**
    * Fees could go to a single miner(forger) usually, but can go to many parties, e.g. see
    * Bentov's Proof-of-Activity proposal http://eprint.iacr.org/2014/452.pdf
    */
  def feesDistribution(block: Block): Map[Prop, Long]

  /**
    * Get block producers(miners/forgers). Usually one miner produces a block, but in some proposals not
    * (see e.g. Meni Rosenfeld's Proof-of-Activity paper http://eprint.iacr.org/2014/452.pdf)
    * @param block
    * @return
    */
  def producers(block: Block): Seq[Prop]

  def blockScore(block: Block)(implicit transactionModule: TransactionModule): BigInt

  def generateNextBlock[TransactionalBlockData](transactionModule: TransactionModule): Future[Option[Block]]

  def generateNextBlocks[TransactionalBlockData](transactionModule: TransactionModule): Future[Seq[Block]]
    //Future.sequence(accounts.map(acc => generateNextBlock(acc))).map(_.flatten)

  def consensusBlockData(block: Block): ConsensusBlockData

  def id(block: Block): BlockId

  def parentId(block: Block): BlockId


  val MaxRollback: Int

  val history: History

  //Append block to current state
  def appendBlock(block: Block): Try[Unit] = synchronized {
    history.appendBlock(block).map { blocks =>
      blocks foreach { b =>
        transactionModule.state.processBlock(b) match {
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
          transactionModule.state.rollbackTo(height)
        case None =>
          log.warn(s"RemoveAfter non-existing block ${Base58.encode(id)}")
      }
      case _ =>
        throw new RuntimeException("Not available for other option than linear blockchain")
    }
  }
}