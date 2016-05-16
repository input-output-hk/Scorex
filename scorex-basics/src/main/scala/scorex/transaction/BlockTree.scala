package scorex.transaction

import scorex.utils.ScorexLogging

trait BlockTree[TX <: Transaction[_]] extends History[TX] with ScorexLogging
