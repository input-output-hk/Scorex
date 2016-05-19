package scorex.transaction.proof

case class AndProofs(proofs: (Proof, Proof)) extends Proof {
  override val proofId = 20: Byte
  override lazy val proofBytes = proofs._1.bytes ++ proofs._2.bytes
}