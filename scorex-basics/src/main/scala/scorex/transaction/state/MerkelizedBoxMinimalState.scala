package scorex.transaction.state

import scorex.crypto.authds.merkle.versioned.MvStoreVersionedMerklizedSeq
import scorex.crypto.hash.CryptographicHash
import scorex.transaction.box.Proposition

import scala.util.Try

//todo: implement
//we assume only boxed state could be Merkelized
trait MerkelizedBoxMinimalState[L <: Proposition, HashFunction <: CryptographicHash]
  extends BoxMinimalState[L] {

  private val boxesStorage = MvStoreVersionedMerklizedSeq
  val hashFunction: HashFunction

  override private[transaction] def rollbackTo(height: Int): Try[MerkelizedBoxMinimalState[L, HashFunction]]
}