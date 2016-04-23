package scorex.transaction.box

import scorex.transaction.Transaction

/**
  *
  * Transaction changes state of existing boxes and creates new ones
  */

trait BoxTransaction extends Transaction {
  val modifiers: Seq[BoxUnlocker[_]]

  val newBoxes: Seq[Box[_]]

  def minFee(currentHeight: Int): Int = newBoxes.map(_.minFee(currentHeight)).sum
}