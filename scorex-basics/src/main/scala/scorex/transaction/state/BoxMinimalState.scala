package scorex.transaction.state

import scorex.transaction.box.{Lock, Box}

trait BoxMinimalState[L <: Lock] extends MinimalState[Box[L]]
