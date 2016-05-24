package scorex.transaction.state

import scorex.serialization.BytesSerializable

abstract class StateElement extends BytesSerializable {
  val publicKey: Array[Byte]

  val SignatureLength: Byte

  def verify(message: Array[Byte], signature: Array[Byte], nonce: Array[Byte]): Boolean
}

abstract class StateElementOwner[SECRET, SE <: StateElement] extends BytesSerializable {
  val secret: SECRET

  override val bytes: Array[Byte]

  def sign(message: Array[Byte]):Array[Byte]
}