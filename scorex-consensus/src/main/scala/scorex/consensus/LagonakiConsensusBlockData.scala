package scorex.consensus

import scorex.block.{Block, ConsensusData, TransactionalData}
import scorex.crypto.signatures.Curve25519
import scorex.transaction.LagonakiTransaction
import scorex.transaction.box.PublicKey25519Proposition

trait LagonakiConsensusBlockData extends ConsensusData {

  override val BlockIdLength: Int = Curve25519.SignatureLength25519

  val blockId: Array[Byte]

  val parentId: Array[Byte]

  val signature: Array[Byte]

  val producer: PublicKey25519Proposition

  assert(blockId.length == BlockIdLength)

  assert(parentId.length == BlockIdLength)
}


class LagonakiBlock[CData <: LagonakiConsensusBlockData, TData <: TransactionalData[_]]
(
  override val version: Byte,
  override val timestamp: Long,
  override val consensusData: CData,
  override val transactionalData: TData)
  extends Block[PublicKey25519Proposition, CData, TData](version, timestamp, consensusData, transactionalData)