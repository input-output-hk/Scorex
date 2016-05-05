package scorex.transaction.box

sealed trait Lock {
  val bytes: Array[Byte]
}

sealed trait SigmaLock extends Lock {
  val x: Array[Byte]

  override val bytes = x
}

case class HeightOpenLock(height: Int)
