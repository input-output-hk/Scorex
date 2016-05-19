package scorex.transaction.proof

case object NoProof extends Proof {
  override val proofId = 1: Byte
  override val proofBytes: Array[Byte] = Array()
}
