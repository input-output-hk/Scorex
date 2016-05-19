package scorex.transaction.box

import scorex.transaction.proof.Proof

trait BoxProof[L <: Proposition] extends Proof

trait SigmaProof[SL <: SigmaProposition] extends BoxProof[SL] {
  val z: Array[Byte]
}