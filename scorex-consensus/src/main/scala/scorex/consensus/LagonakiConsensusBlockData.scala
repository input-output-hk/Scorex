package scorex.consensus

import scorex.crypto.signatures.Curve25519

trait LagonakiConsensusBlockData {

  val BlockIdLength: Int = Curve25519.SignatureLength25519

  val blockId: Array[Byte]

  val parentId: Array[Byte]

  assert(blockId.length == BlockIdLength)

  assert(parentId.length == BlockIdLength)
}
