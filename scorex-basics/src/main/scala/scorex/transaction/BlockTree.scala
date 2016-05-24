package scorex.transaction

import scorex.transaction.state.StateElement
import scorex.utils.ScorexLogging

trait BlockTree[SE <: StateElement, TX <: Transaction[SE]] extends History[SE, TX] with ScorexLogging
