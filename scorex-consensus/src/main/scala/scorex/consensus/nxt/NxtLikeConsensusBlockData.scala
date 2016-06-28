package scorex.consensus.nxt

import com.google.common.primitives.{Bytes, Longs}
import scorex.consensus.LagonakiConsensusBlockData

trait NxtLikeConsensusBlockData extends LagonakiConsensusBlockData {
  val baseTarget: Long
  val generationSignature: Array[Byte]

  def bytes: Array[Byte] =
    Bytes.ensureCapacity(Longs.toByteArray(baseTarget), 8, 0) ++ generationSignature
}
