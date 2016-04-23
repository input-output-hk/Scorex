package scorex.transaction.box

import scorex.crypto.hash.SecureCryptographicHash

sealed trait Unlocker[L <: Lock]

sealed trait SigmaUnlocker[SL <: SigmaLock] {
  // new boxes common hash
  val newBoxesHash: Array[Byte]

  lazy val a = newBoxesHash
  lazy val e = SecureCryptographicHash.hash(a)
}