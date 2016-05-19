package scorex.transaction.proof

case class OrProofs(proofs: (Proof, Proof)) extends Proof {
  override val proofId = 30: Byte
  override lazy val proofBytes = proofs._1.bytes ++ proofs._2.bytes
}