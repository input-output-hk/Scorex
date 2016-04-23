package scorex.transaction.box

trait BoxUnlocker[L <: Lock] {
  val box: Box[L]
  val key: BoxProof[L]
}