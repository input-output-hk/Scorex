package scorex.transaction.proof

import scorex.serialization.BytesSerializable

/**
  * The most general abstraction of fact a prover can provide a non-interactive proof
  * to open a box or to modify an account
  */

trait Proof extends BytesSerializable {

  def proofId: Byte

  def proofBytes: Array[Byte]

  /**
    * The proof is non-interactive and thus serializable
    */
  //todo: include id:  Array(proofId) ++ proofBytes
  lazy val bytes =  proofBytes
}
