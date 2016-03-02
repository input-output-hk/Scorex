package scorex.transaction

import play.api.libs.json.JsObject
import scorex.account.Account


/**
  * A transaction is an atomic state modifier
  */

trait Transaction extends StateChangeReason {
  val fee: Long

  val timestamp: Long
  val recipient: Account

  /**
    * A transaction could be serialized into JSON
    */
  def json: JsObject
}


/**
  * New structures
  */


//todo: state events, any crypto, ZKPs,

sealed trait Lock

case object NoLock extends Lock

case class ToPreimage(hash: Array[Byte]) extends Lock

case class ToPubKey(pubKey: Array[Byte]) extends Lock

case class And(lock1: Lock, Lock2: Lock) extends Lock

/**
  * Box is a state element. Basically it is some value locked by some state machine.
  */
trait Box {
  val locks: Seq[Lock]

  val memo: Array[Byte]
  val value: Long

  val validationLevel: Any
  val validationType: Any
  val validationClass: Any
}

/**
  * todo: modifier
  */

trait BoxModifier {
  val box: Box
  val key: Any
}

/**
  *
  * Transaction changes state of existing boxes and creates new ones
  */
trait NewTransaction {
  val fee: Long

  val timestamp: Long

  val modifiers: Seq[BoxModifier]

  val newBoxes: Seq[Box]
}