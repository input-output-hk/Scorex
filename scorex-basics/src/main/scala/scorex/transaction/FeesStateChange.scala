package scorex.transaction

import com.google.common.primitives.Longs
import scorex.transaction.proof.NoProof

case class FeesStateChange(fee: Long) extends StateChangeReason {
  override lazy val proof = NoProof

  override def messageToSign: Array[Byte] = Longs.toByteArray(fee)

  override def correctAuthorship(): Boolean = true
}