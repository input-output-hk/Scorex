package scorex.transaction.box

trait BoxUnlocker[L <: Proposition] {
  val closedBox: Box[L]
  val key: BoxProof[L]
}