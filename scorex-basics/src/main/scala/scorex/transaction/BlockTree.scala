package scorex.transaction

import scorex.utils.ScorexLogging

trait BlockTree[TX <: Transaction] extends History[TX] with ScorexLogging
