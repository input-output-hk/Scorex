package scorex.transaction

import com.google.common.primitives.Longs

case class FeesStateChange(fee: Long) extends StateChangeReason {
  override lazy val bytes: Array[Byte] = Longs.toByteArray(fee)

  override lazy val serializedProof: Array[Byte] = Array()
}