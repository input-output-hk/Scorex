package scorex.transaction.state

import scorex.crypto.hash.CryptographicHash

class Lazy[A](operation: => A) {
  lazy val get = operation
}

trait AuthenticatedState[ELEM <: StateElement] extends MinimalState[ELEM] {
  val proof: Array[Byte]

  val elements: Seq[Lazy[ELEM]]
}


trait MerklizedState[ELEM <: StateElement, HashFunction <: CryptographicHash]
  extends AuthenticatedState[ELEM]{

  val hashFunction: HashFunction
}