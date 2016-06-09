package scorex.transaction.state

import scorex.serialization.BytesSerializable
import scorex.transaction.box.{Proposition, Box}
import scala.util.Try

trait SecretHolder[P <: Proposition] extends BytesSerializable {
  type Secret

  val publicCommitment: P

  val secret: Secret

  def owns(stateElement: Box[P]): Boolean

  def sign(message: Array[Byte]): Array[Byte]

  //def verify(message: Array[Byte], signature: Array[Byte], nonce: Array[Byte]): Boolean
}


trait SecretHolderGenerator[P <: Proposition, SH <: SecretHolder[P]] {
  def generateKeys(randomSeed: Array[Byte]): SH

  def parse(bytes: Array[Byte]): Try[SH]
}