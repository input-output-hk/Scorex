package scorex.transaction.box

import com.google.common.primitives.Ints

sealed trait Proposition {
  val encodedType: Byte

  protected val specificBytes: Array[Byte]

  final val bytes: Array[Byte] = Array(encodedType) ++ specificBytes
}

sealed trait SigmaProposition extends Proposition {
  val a: Array[Byte]

  override val specificBytes = a
}

case class HeightOpenProposition(height: Int) extends Proposition {
  override val encodedType = 0: Byte
  override val specificBytes = Ints.toByteArray(height)
}
