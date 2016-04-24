package scorex.transaction.state

import scorex.crypto.hash.CryptographicHash
import scorex.transaction.box.Lock

//todo: implement
//we assume only boxed state could be Merkelized
trait MerkelizedBoxMinimalState[L <: Lock, HashFunction <: CryptographicHash] extends BoxMinimalState[L] {
  val hashFunction: HashFunction
}

