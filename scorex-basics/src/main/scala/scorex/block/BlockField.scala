package scorex.block

import com.google.common.primitives.{Bytes, Ints, Longs}
import play.api.libs.json.{JsObject, Json}
import scorex.crypto.encode.Base58
import scorex.serialization.{JsonSerializable, BytesSerializable}
import scorex.transaction.Transaction
import scorex.transaction.box.Proposition

/**
  * An abstraction of a part of a block, wrapping some data. The wrapper interface
  * provides binary & json serializations.
  *
  * @tparam T - type of a value wrapped into a blockfield
  */
abstract class BlockField[T] extends BytesSerializable with JsonSerializable {
  val name: String
  val value: T

}

case class ByteBlockField(override val name: String, override val value: Byte) extends BlockField[Byte] {

  override lazy val json: JsObject = Json.obj(name -> value.toInt)
  override lazy val bytes: Array[Byte] = Array(value)
}

case class IntBlockField(override val name: String, override val value: Int) extends BlockField[Int] {

  override lazy val json: JsObject = Json.obj(name -> value)
  override lazy val bytes: Array[Byte] = Bytes.ensureCapacity(Ints.toByteArray(value), 4, 0)
}

case class LongBlockField(override val name: String, override val value: Long) extends BlockField[Long] {

  override lazy val json: JsObject = Json.obj(name -> value)
  override lazy val bytes: Array[Byte] = Bytes.ensureCapacity(Longs.toByteArray(value), 8, 0)
}

case class BlockIdField(override val name: String, override val value: Block.BlockId)
  extends BlockField[Block.BlockId] {

  override lazy val json: JsObject = Json.obj(name -> Base58.encode(value))
  override lazy val bytes: Array[Byte] = value
}

case class TransactionBlockField(override val name: String, override val value: Transaction[_ <: Proposition])
  extends BlockField[Transaction[_ <: Proposition]] {

  override lazy val json: JsObject = value.json
  override lazy val bytes: Array[Byte] = value.bytes
}

/*
case class ProducerBlockField(override val name: String, override val value: Identifiable)
  extends BlockField[Identifiable] {

  override lazy val json: JsObject = Json.obj("generator" -> value.toString)

  override lazy val bytes: Array[Byte] = value.id
} */

case class SignatureBlockField(override val name: String, override val value: Array[Byte])
  extends BlockField[Array[Byte]] {

  override lazy val json: JsObject = Json.obj("signature" -> Base58.encode(value))

  override lazy val bytes: Array[Byte] = value
}