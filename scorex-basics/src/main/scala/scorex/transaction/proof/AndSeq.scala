package scorex.transaction.proof

import com.google.common.primitives.Ints

case class AndSeq(proofs: Seq[Proof]) extends Proof {
  override lazy val bytes =
    proofs.foldLeft(Ints.toByteArray(proofs.size)) { case (bs, proof) =>
      bs ++ Ints.toByteArray(proof.bytes.length) ++ proof.bytes
    }
}
