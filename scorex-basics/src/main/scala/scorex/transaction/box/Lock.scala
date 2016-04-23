package scorex.transaction.box


sealed trait Lock {
  val bytes: Array[Byte]
}

sealed trait SigmaLock extends Lock {
  val x: Array[Byte]
}

case class HeightOpenLock(height: Int)
