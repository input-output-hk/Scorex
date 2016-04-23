package scorex.transaction

import com.google.common.primitives.Ints


/**
  * A proof
  */

trait Proof {
  val bytes: Array[Byte]
}

case class Signature(signature: Array[Byte]) extends Proof {
  override lazy val bytes: Array[Byte] = signature
}

case class AndSeq(proofs: Seq[Proof]) extends Proof {
  override lazy val bytes =
    proofs.foldLeft(Ints.toByteArray(proofs.size)) { case (bs, proof) =>
      bs ++ Ints.toByteArray(proof.bytes.length) ++ proof.bytes
    }
}

case object NoProof extends Proof {
  override val bytes: Array[Byte] = Array()
}

/**
  * A reason to change a system state
  */
trait StateChangeReason extends Serializable {
  /**
    * A reason could be serialized into a binary form
    */
  val bytes: Array[Byte]

  val proof: Proof
}