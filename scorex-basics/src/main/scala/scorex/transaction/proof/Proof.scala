package scorex.transaction.proof

import scorex.serialization.BytesSerializable

/**
  * The most general abstraction of fact a prover can provide a non-interactive proof
  * to open a box or to modify an account
  *
  * A proof is non-interactive and thus serializable
  */

trait Proof extends BytesSerializable {

  def proofId: Byte

  def proofBytes: Array[Byte]

  //todo: include id:  Array(proofId) ++ proofBytes
  override lazy val bytes = proofBytes
}
