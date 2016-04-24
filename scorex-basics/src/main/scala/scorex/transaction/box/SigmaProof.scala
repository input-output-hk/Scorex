package scorex.transaction.box

import scorex.crypto.hash.SecureCryptographicHash
import scorex.transaction.proof.Proof

trait BoxProof[L <: Lock] extends Proof

trait SigmaProof[SL <: SigmaLock] extends BoxProof[SL] {
  val a:Array[Byte]
  lazy val e = SecureCryptographicHash.hash(a)
}