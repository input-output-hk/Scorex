package scorex.transaction

import com.google.common.primitives.Longs
import scorex.transaction.proof.NoProof

case class FeesStateChange(fee: Long) extends StateChangeReason with Serializable {
  override lazy val bytes: Array[Byte] = Longs.toByteArray(fee)

  override lazy val proof = NoProof
}