package scorex.transaction.box

import scorex.crypto.hash.SecureCryptographicHash

sealed trait Unlocker[L <: Lock]

sealed trait SigmaUnlocker[SL <: SigmaLock] {
  // new boxes common hash
  val newBoxesHash: Array[Byte]

  def e(a: Array[Byte]): Array[Byte] = SecureCryptographicHash.hash(newBoxesHash ++ a)
}