package scorex.transaction

import com.google.common.primitives.Longs
import play.api.libs.json.JsObject
import scorex.serialization.{BytesSerializable, JsonSerializable}
import scorex.transaction.box.{Box, BoxUnlocker, Proposition}
import scorex.transaction.state.MinimalState

/**
  * A transaction is an atomic state modifier
  */

abstract class Transaction[P <: Proposition] extends BytesSerializable with JsonSerializable {
  def fee: Long

  val timestamp: Long

  /**
    * A transaction could be serialized into JSON
    * A Transaction opens existing boxes and creates new ones
    */
  def json: JsObject


  /**
    * A transaction is valid against a state if:
    * - boxes a transaction is opening are stored in the state as closed
    * - sum of values of closed boxes = sum of values of open boxes - fee
    * - all the signatures for open boxes are valid(against all the txs bytes except of sigs)
    *
    * - fee >= 0
    *
    * specific semantic rules are applied
    *
    * @param state - state to check a transaction against
    * @return
    */
  def isValid(state: MinimalState[P]): Boolean = {
    val statelessValid = fee >= 0

    val statefulValid = {
      val boxesSumOpt = unlockers.foldLeft[Option[Long]](Some(0L)) { case (partialRes, unlocker) =>
        partialRes.flatMap { partialSum =>
          state.closedBox(unlocker.closedBoxId).flatMap { box =>
            unlocker.boxKey.isValid(box.lock, messageToSign) match {
              case true => Some(partialSum + box.value)
              case false => None
            }
          }
        }
      }

      boxesSumOpt match {
        case Some(openSum) => newBoxes.map(_.value).sum == openSum - fee
        case None => false
      }
    }
    statefulValid && statelessValid && semanticValidity
  }

  def semanticValidity: Boolean


}

abstract class BoxTransaction[P <: Proposition] extends Transaction {
  val unlockers: Traversable[BoxUnlocker[P]]
  val newBoxes: Traversable[Box[P]]

  lazy val messageToSign: Array[Byte] =
    newBoxes.map(_.bytes).reduce(_ ++ _) ++
      unlockers.map(_.closedBoxId).reduce(_ ++ _) ++
      Longs.toByteArray(timestamp) ++
      Longs.toByteArray(fee)
}