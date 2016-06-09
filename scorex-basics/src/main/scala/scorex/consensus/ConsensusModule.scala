package scorex.consensus

import play.api.libs.json.Json
import scorex.block.{BlockIdField, Block, BlockProcessingModule}
import scorex.transaction.box.Proposition
import scorex.transaction.{Transaction, TransactionModule}
import shapeless.{Nat, Sized}
import scala.concurrent.Future


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

trait SingleProducerConsensusBlockData extends BasicConsensusBlockData {
  //val producerField: ProducerBlockField
  //val signatureField: SignatureBlockField
  //lazy val history = transactionModule.blockStorage.history.contains(referenceField.value)
  //lazy val sigValid = producer.verify(messageToSign, signature, nonce)
}


trait ConsensusModule[ConsensusBlockData <: BasicConsensusBlockData, TX <: Transaction] extends BlockProcessingModule[ConsensusBlockData] {

  type BlockId = Array[Byte]
  val BlockIdLength: Int

  def isValid[TransactionalBlockData](block: Block[TX])(implicit transactionModule: TransactionModule[TransactionalBlockData, TX]): Boolean

  /**
    * Fees could go to a single miner(forger) usually, but can go to many parties, e.g. see
    * Bentov's Proof-of-Activity proposal http://eprint.iacr.org/2014/452.pdf
    */
  def feesDistribution(block: Block[TX]): Map[Proposition, Long]

  /**
    * Get block producers(miners/forgers). Usually one miner produces a block, but in some proposals not
    * (see e.g. Meni Rosenfeld's Proof-of-Activity paper http://eprint.iacr.org/2014/452.pdf)
    * @param block
    * @return
    */
  def producers(block: Block[TX]): Seq[Proposition]

  def blockScore(block: Block[TX])(implicit transactionModule: TransactionModule[_, TX]): BigInt

  def generateNextBlock[TransactionalBlockData](transactionModule: TransactionModule[TransactionalBlockData, TX]): Future[Option[Block[TX]]]

  def generateNextBlocks[TransactionalBlockData](transactionModule: TransactionModule[TransactionalBlockData, TX]): Future[Seq[Block[TX]]]
    //Future.sequence(accounts.map(acc => generateNextBlock(acc))).map(_.flatten)

  def consensusBlockData(block: Block[TX]): ConsensusBlockData

  def id(block: Block[TX]): BlockId

  def parentId(block: Block[TX]): BlockId
}