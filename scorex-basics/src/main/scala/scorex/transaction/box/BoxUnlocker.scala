package scorex.transaction.box

trait BoxUnlocker[L <: Proposition] {
  val box: Box[L]
  val key: BoxProof[L]
}