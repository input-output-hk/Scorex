package scorex.lagonaki.server

import scorex.consensus.{ConsensusSettings}
import scorex.settings.Settings

class LagonakiSettings(override val filename: String) extends Settings with ConsensusSettings
