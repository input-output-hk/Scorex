package scorex.transaction

import play.api.libs.json.JsObject
import scorex.serialization.JsonSerializable
import scorex.transaction.account.Account
import scorex.transaction.box.{Box, BoxUnlocker, Proposition}
import scorex.transaction.state.StateElement


/**
  * A transaction is an atomic state modifier
  */

sealed abstract class Transaction[SE <: StateElement] extends StateChangeReason with JsonSerializable {
  def fee: Long

  val timestamp: Long

  /**
    * A transaction could be serialized into JSON
    */
  def json: JsObject
}


abstract class AccountTransaction extends Transaction[Account] {
  val sender: Option[Account]
  val recipient: Account
}

/**
  *
  * A BoxTransaction opens existing boxes and creates new ones
  */
abstract class BoxTransaction[Prop <: Proposition] extends Transaction[Box[Prop]] {
  val unlockers: Seq[BoxUnlocker[Prop]]

  val newBoxes: Seq[Box[Prop]]

  override lazy val fee: Long = newBoxes.map(_.fee).sum

  override lazy val messageToSign: Array[Byte] =
    newBoxes.map(_.bytes).reduce(_ ++ _) ++
      unlockers.map(_.closedBox.id).reduce(_ ++ _)
}

