package scorex.transaction.box

import com.google.common.primitives.Ints

sealed trait Proposition {
  val encodedType: Byte

  protected val speficicBytes: Array[Byte]

  final val bytes: Array[Byte] = Array(encodedType) ++ speficicBytes
}

sealed trait SigmaProposition extends Proposition {
  val x: Array[Byte]

  override val speficicBytes = x
}

case class HeightOpenProposition(height: Int) extends Proposition {
  override val encodedType = 0: Byte
  override val speficicBytes = Ints.toByteArray(height)
}
