package scorex.consensus.qora

import com.google.common.primitives.{Bytes, Longs}
import scorex.transaction.account.BalanceSheet
import scorex.block.{Block, TransactionalData}
import scorex.consensus.blockchain.StoredBlockchain
import scorex.consensus.nxt.NxtLikeConsensusBlockData
import scorex.consensus.{BlockChain, ConsensusModule, History, LagonakiConsensusModule}
import scorex.crypto.hash.FastCryptographicHash._
import scorex.transaction._
import scorex.transaction.box.PublicKey25519Proposition
import scorex.transaction.state.PrivateKey25519Holder
import scorex.transaction.state.PrivateKey25519Holder.PrivateKey25519
import scorex.utils.NTP

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try


class QoraLikeConsensusModule[TX <: Transaction[PublicKey25519Proposition, TX], TData <: TransactionalData[TX]]
  extends LagonakiConsensusModule[QoraLikeConsensusBlockData, QoraBlock[TX, TData]]
    with StoredBlockchain[PublicKey25519Proposition, TX, QoraLikeConsensusBlockData, QoraBlock[TX, TData]]{

  import QoraLikeConsensusModule.GeneratorSignatureLength

  val GeneratingBalanceLength = 8

  private val ReTarget = 10
  private val MinBalance = 1L
  private val MaxBalance = 10000000000L

  private val MinBlockTime = 1.minute.toSeconds
  private val MaxBlockTime = 5.minute.toSeconds


  def calculateSignature(prevBlock: QoraBlock[TX, TData], account: PrivateKey25519Holder): Array[Byte] = {
    val gb = getNextBlockGeneratingBalance(prevBlock)
    val ref = prevBlock.generatorSignature
    calculateSignature(ref, gb, account)
  }

  def calculateSignature(reference: Array[Byte],
                         generatingBalance: Long,
                         account: PrivateKey25519Holder): Array[Byte] = {
    val generatorSignature = reference.take(GeneratorSignatureLength)

    val genBalanceBytes = Longs.toByteArray(generatingBalance)
      .ensuring(_.size == GeneratingBalanceLength)

    val si = Bytes.concat(generatorSignature, genBalanceBytes, account.publicCommitment.bytes)
    account.sign(si).proofBytes
  }

  def getBaseTarget(generatingBalance: Long): BigInt = BigInt(minMaxBalance(generatingBalance)) * getBlockTime(generatingBalance)

  def getBlockTime(generatingBalance: Long): Long = {
    val percentageOfTotal = minMaxBalance(generatingBalance) / MaxBalance.toDouble
    (MinBlockTime + ((MaxBlockTime - MinBlockTime) * (1 - percentageOfTotal))).toLong
  }

  private def minMaxBalance(generatingBalance: Long) =
    if (generatingBalance < MinBalance) MinBalance
    else if (generatingBalance > MaxBalance) MaxBalance
    else generatingBalance

  private def blockGeneratingBalance(block: QoraBlock[TX, TData]) = block.generatingBalance

  def getNextBlockGeneratingBalance(block: QoraBlock[TX, TData]): Long = {
    if (heightOf(block).get % ReTarget == 0) {
      //GET FIRST BLOCK OF TARGET
      val firstBlock = (1 to ReTarget - 1).foldLeft(block) { case (bl, _) =>
        parent(bl).get
      }

      //CALCULATE THE GENERATING TIME FOR LAST 10 BLOCKS
      val generatingTime = block.timestamp - firstBlock.timestamp

      //CALCULATE EXPECTED FORGING TIME
      val expectedGeneratingTime = getBlockTime(blockGeneratingBalance(block)) * ReTarget * 1000

      //CALCULATE MULTIPLIER
      val multiplier = expectedGeneratingTime / generatingTime.toDouble

      //CALCULATE NEW GENERATING BALANCE
      val generatingBalance = (blockGeneratingBalance(block) * multiplier).toLong
      minMaxBalance(generatingBalance)
    } else blockGeneratingBalance(block)
  }

  def getNextBlockGeneratingBalance(): Long = {
    getNextBlockGeneratingBalance(lastBlock)
  }

  override def generateNextBlock(account: PrivateKey25519Holder)
                                    (implicit transactionModule: TransactionModule[PublicKey25519Proposition, TX, TData]): Future[Option[QoraBlock[TX, TData]]] = {
    val version = 1: Byte

    require(transactionModule.isInstanceOf[BalanceSheet[PublicKey25519Proposition]])

    //todo: asInstanceOf
    val generationBalance = transactionModule.asInstanceOf[BalanceSheet[PublicKey25519Proposition]].generationBalance(account.publicCommitment)
    require(generationBalance > 0, "Zero generating balance in generateNextBlock")

    val signature = calculateSignature(lastBlock, account)
    val h = hash(signature)
    val hashValue = BigInt(1, h)

    //CALCULATE ACCOUNT TARGET
    val targetBytes = Array.fill(32)(Byte.MaxValue)
    val baseTarget = getBaseTarget(getNextBlockGeneratingBalance(lastBlock))
    //MULTIPLY TARGET BY USER BALANCE
    val target = BigInt(1, targetBytes) / baseTarget * BigInt(generationBalance)

    //CALCULATE GUESSES
    val guesses = hashValue / target + 1

    //CALCULATE TIMESTAMP
    val timestampRaw = guesses * 1000 + lastBlock.timestamp

    //CHECK IF NOT HIGHER THAN MAX LONG VALUE
    val timestamp = if (timestampRaw > Long.MaxValue) Long.MaxValue else timestampRaw.longValue()

    if (timestamp <= NTP.correctedTime()) {

      val consensusData = new QoraLikeConsensusBlockData {
        override val generatorSignature: Array[Byte] = signature
        override val generatingBalance: Long = getNextBlockGeneratingBalance(lastBlock)
      }
      Future(Some(Block.buildAndSign(version,
        timestamp,
        id(lastBlock),
        consensusData,
        transactionModule.packUnconfirmed(),
        account)))
    } else Future(None)
  }

  def parseBytes(bytes: Array[Byte]): Try[Unit] = Try {
    /*new QoraLikeConsensusBlockData {
      override val generatingBalance: Long = Longs.fromByteArray(bytes.take(GeneratingBalanceLength))
      override val generatorSignature: Array[Byte] = bytes.takeRight(GeneratorSignatureLength)
    }*/
  }


  override def isValid(block: QoraBlock[TX, TData])(implicit transactionModule: TransactionModule[_, _, _]): Boolean = {
    val data = block.consensusData

    if (data.generatingBalance != getNextBlockGeneratingBalance(parent(block).get)) {
      //CHECK IF GENERATING BALANCE IS CORRECT
      false
    } else {
      //target base
      val targetBytes = Array.fill(32)(Byte.MaxValue)
      val baseTarget: BigInt = getBaseTarget(data.generatingBalance)
      val gen = data.producer
      val genBalance = BigInt(transactionModule.asInstanceOf[BalanceSheet[PublicKey25519Proposition]].generationBalance(gen))
      val target0 = BigInt(1, targetBytes) / baseTarget * genBalance

      //target bounds
      val guesses = (block.timestamp - parent(block).get.timestamp) / 1000
      val lowerTarget = target0 * (guesses - 1)
      val target = target0 * guesses

      val hit = BigInt(1, hash(data.generatorSignature))

      //generation check
      hit >= lowerTarget && hit < target
    }
  }

  override def genesisData: QoraLikeConsensusBlockData =
    QoraLikeConsensusBlockData(

      generatingBalance = 10000000L
      generatorSignature = Array.fill(64)(0: Byte)
  )

  override def blockScore(block: QoraBlock[TX, TData])(implicit transactionModule: TransactionModule[PublicKey25519Proposition, _, _]): BigInt = BigInt(1)
}

object QoraLikeConsensusModule {
  val GeneratorSignatureLength = 64
}