package scorex.transaction.box


sealed trait Lock {
  val bytes: Array[Byte]
}

sealed trait SigmaLock extends Lock {
  val a: Array[Byte]
}

case class HeightOpenLock(height: Int)
