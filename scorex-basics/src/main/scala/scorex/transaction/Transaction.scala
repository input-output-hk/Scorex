package scorex.transaction

import play.api.libs.json.JsObject

/**
  * A transaction is an atomic state modifier
  */

trait Transaction extends StateChangeReason {
  val fee: Long

  val timestamp: Long

  /**
    * A transaction could be serialized into JSON
    */
  def json: JsObject
}