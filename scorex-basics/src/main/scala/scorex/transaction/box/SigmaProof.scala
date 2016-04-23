package scorex.transaction.box

import scorex.crypto.hash.SecureCryptographicHash
import scorex.transaction.Proof

trait BoxProof[L <: Lock] extends Proof

trait SigmaProof[SL <: SigmaLock] extends BoxProof[SL] {
  // new boxes common hash
  val newBoxesHash: Array[Byte]

  lazy val a = newBoxesHash
  lazy val e = SecureCryptographicHash.hash(a)
}