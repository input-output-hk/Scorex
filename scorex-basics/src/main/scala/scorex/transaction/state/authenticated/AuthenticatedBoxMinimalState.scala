package scorex.transaction.state.authenticated

import scorex.crypto.authds.storage.StorageType
import scorex.crypto.authds.{AuthenticatedDictionary, DataProof}
import scorex.crypto.hash.CryptographicHash
import scorex.transaction.box.Proposition
import scorex.transaction.state.MinimalState

import scala.util.Try


trait AuthenticatedBoxMinimalState[L <: Proposition, HashFunction <: CryptographicHash]
  extends MinimalState[L] {

  type ElementProof <: DataProof
  type Storage <: StorageType

  protected val boxesStorage: AuthenticatedDictionary[ElementProof, Storage]

  def digest: HashFunction#Digest

  val hashFunction: HashFunction

  override def rollbackTo(height: Int): Try[AuthenticatedBoxMinimalState[L, HashFunction]]
}