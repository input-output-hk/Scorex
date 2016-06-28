package scorex.consensus.qora

import com.google.common.primitives.{Bytes, Longs}
import scorex.block.{Block, TransactionalData}
import scorex.consensus.LagonakiConsensusBlockData
import scorex.transaction.Transaction
import scorex.transaction.box.PublicKey25519Proposition
import shapeless.HNil

case class QoraLikeConsensusBlockData(
                                       blockId: Array[Byte],
                                       parentId: Array[Byte],
                                       generatingBalance: Long,
                                       generatorSignature: Array[Byte],
                                       producer: PublicKey25519Proposition,
                                       signature: Array[Byte]) extends LagonakiConsensusBlockData {

  override val consensusFields = blockId :: parentId :: generatingBalance :: generatorSignature :: producer :: signature :: HNil

  //todo: fix
  def bytes: Array[Byte] =
    Bytes.ensureCapacity(Longs.toByteArray(generatingBalance), 8, 0) ++ generatorSignature
}


case class QoraBlock[TX <: Transaction[PublicKey25519Proposition, TX], TData <: TransactionalData[TX]](
                                                                                                   override val version: Byte,
                                                                                                   override val timestamp: Long,
                                                                                                   blockId: Array[Byte],
                                                                                                   parentId: Array[Byte],
                                                                                                   generatingBalance: Long,
                                                                                                   generatorSignature: Array[Byte],
                                                                                                   producer: PublicKey25519Proposition,
                                                                                                   signature: Array[Byte],
                                                                                                   override val transactionalData: TData)
  extends Block[PublicKey25519Proposition, QoraLikeConsensusBlockData, TData](version, timestamp,
    QoraLikeConsensusBlockData(blockId, parentId, generatingBalance, generatorSignature,
      producer, signature: Array[Byte]), transactionalData)