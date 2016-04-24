package scorex.transaction.box

import scorex.transaction.Transaction

/**
  *
  * A BoxTransaction opens existing boxes and creates new ones
  */
trait BoxTransaction extends Transaction {
  val unlockers: Seq[BoxUnlocker[_]]

  val newBoxes: Seq[Box[_]]

  lazy val fee: Long = newBoxes.map(_.fee).sum
}