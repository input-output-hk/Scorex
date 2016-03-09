package scorex.transaction.box

import scorex.crypto.hash.FastCryptographicHash


sealed trait Unlocker[L <: Lock]

sealed trait SigmaUnlocker[SL <: SigmaLock] {
  // new boxes common hash
  val newBoxesHash: Array[Byte]

  def e(a: Array[Byte]): Array[Byte] = FastCryptographicHash.hash(newBoxesHash ++ a)
}
