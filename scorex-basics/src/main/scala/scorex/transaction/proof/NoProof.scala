package scorex.transaction.proof

case object NoProof extends Proof {
  override val bytes: Array[Byte] = Array()
}
