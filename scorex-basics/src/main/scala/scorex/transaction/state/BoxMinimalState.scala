package scorex.transaction.state

import scorex.transaction.box.{Proposition, Box}

trait BoxMinimalState[L <: Proposition] extends MinimalState[Box[L]]
