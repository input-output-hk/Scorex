package scorex.transaction

import play.api.libs.json.JsObject
import scorex.transaction.account.Account
import scorex.transaction.box.{Box, BoxUnlocker, Proposition}
import scorex.transaction.proof.special.Signature25519
import scorex.transaction.state.StateElement
import scorex.serialization.JsonSerializable


/**
  * A transaction is an atomic state modifier
  */

sealed abstract class Transaction[SE <: StateElement] extends StateChangeReason with JsonSerializable {
  val fee: Long

  val timestamp: Long

  /**
    * A transaction could be serialized into JSON
    */
  def json: JsObject

  def messageToSign: Array[Byte] = ???
}


abstract class AccountTransaction extends Transaction[Account] {

  import scorex.transaction.proof.Proof

  val recipient: Account
  val signature: Array[Byte]

  override lazy val proof: Proof = Signature25519(signature)
}

/**
  *
  * A BoxTransaction opens existing boxes and creates new ones
  */
abstract class BoxTransaction[Prop <: Proposition] extends Transaction[Box[Prop]] {
  val unlockers: Seq[BoxUnlocker[Prop]]

  val newBoxes: Seq[Box[Prop]]

  lazy val fee: Long = newBoxes.map(_.fee).sum
}

