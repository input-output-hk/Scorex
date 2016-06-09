package scorex.transaction.account

import scorex.transaction.box.{Box, Proposition, PublicKeyProposition}


//todo: rename to StatefulAccount?
trait NoncedBox[P <: Proposition] extends Box[P] {
  val nonce: Long
}


trait PublicKeyNoncedBox[PKP <: PublicKeyProposition] extends NoncedBox[PKP] {
  lazy val id = lock.id  //todo : add nonce

  lazy val publicKey = lock.publicKey

  //todo: probably incorrect
  override def equals(obj: Any): Boolean = obj match {
    case acc: PublicKeyNoncedBox[PKP] => acc.lock == this.lock && acc.value == this.value
    case _ => false
  }

  override def hashCode(): Int = lock.hashCode()
}
