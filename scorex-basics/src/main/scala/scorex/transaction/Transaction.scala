package scorex.transaction

import play.api.libs.json.JsObject
import scorex.serialization.JsonSerializable
import scorex.transaction.box.{PublicKeyProposition, Box, BoxUnlocker, Proposition}

/**
  * A transaction is an atomic state modifier
  */

sealed abstract class Transaction extends Signable with StateChangeReason with JsonSerializable {
  def fee: Long

  val timestamp: Long

  /**
    * A transaction could be serialized into JSON
    */
  def json: JsObject

  val senders: Traversable[Box[_]]
  val recipients: Traversable[Box[_]]
}

abstract class AccountTransaction[PKP <: PublicKeyProposition] extends Transaction {
  override val senders: Traversable[Box[PKP]]
  override val recipients: Traversable[Box[PKP]]
}

/**
  *
  * A BoxTransaction opens existing boxes and creates new ones
  */
abstract class BoxTransaction[Prop <: Proposition] extends Transaction {
  val unlockers: Seq[BoxUnlocker[Prop]]

  override lazy val messageToSign: Array[Byte] =
    recipients.map(_.bytes).reduce(_ ++ _) ++
      unlockers.map(_.closedBox.id).reduce(_ ++ _)
}

