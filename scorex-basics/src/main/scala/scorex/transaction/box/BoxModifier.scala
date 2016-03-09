package scorex.transaction.box

trait BoxModifier[L <: Lock] {
  val box: Box[L]
  val key: Unlocker[L]
}
