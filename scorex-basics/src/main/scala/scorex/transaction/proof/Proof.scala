package scorex.transaction.proof

/**
  * The most general abstraction of fact a prover can provide a non-interactive proof
  * to open a box or to modify an account
  */

trait Proof {

  /**
    * The proof is non-interactive and thus serializable
    */
  val bytes: Array[Byte]
}
