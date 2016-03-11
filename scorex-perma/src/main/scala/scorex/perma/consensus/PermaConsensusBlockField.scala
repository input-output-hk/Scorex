package scorex.perma.consensus

import com.google.common.primitives.{Bytes, Ints, Longs}
import play.api.libs.json._
import scorex.block.BlockField
import scorex.crypto.EllipticCurveImpl
import scorex.crypto.hash.FastCryptographicHash
import scorex.crypto.storage.auth.AuthDataBlock
import scorex.perma.settings.PermaConstants

import scala.annotation.tailrec
import scala.util.Try

case class PermaConsensusBlockField(override val value: PermaConsensusBlockData)
  extends BlockField[PermaConsensusBlockData] {

  import PermaConsensusBlockField._

  override val name: String = PermaConsensusBlockField.FieldName

  override def bytes: Array[Byte] =
    Bytes.ensureCapacity(Ints.toByteArray(value.target.toByteArray.length), 4, 0) ++ value.target.toByteArray ++
      Bytes.ensureCapacity(value.puz, PuzLength, 0) ++
      Bytes.ensureCapacity(value.ticket.publicKey, PublicKeyLength, 0) ++
      Bytes.ensureCapacity(value.ticket.s, SLength, 0) ++
      Bytes.ensureCapacity(Ints.toByteArray(value.ticket.proofs.length), 4, 0) ++
      value.ticket.proofs.foldLeft(Array.empty: Array[Byte]) { (b, p) =>
        val blockBytes = AuthDataBlock.encode(p.segment)
        b ++ Bytes.ensureCapacity(p.signature, SignatureLength, 0) ++ Ints.toByteArray(blockBytes.length) ++ blockBytes
      }

  override def json: JsObject = Json.obj(name -> Json.toJson(value))
}

object PermaConsensusBlockField {

  val FieldName = "perma-consensus"
  val PuzLength = 32
  val PublicKeyLength = EllipticCurveImpl.KeyLength
  val SLength = 32
  val HashLength = FastCryptographicHash.DigestSize
  val SignatureLength = EllipticCurveImpl.SignatureLength

  def parse(bytes: Array[Byte]): Try[PermaConsensusBlockField] = Try {
    @tailrec
    def parseProofs(from: Int, total: Int, current: Int, acc: IndexedSeq[PartialProof]): IndexedSeq[PartialProof] = {
      if (current < total) {
        val proofsStart = from
        val signatureEnd = proofsStart + SignatureLength
        val blockStart = signatureEnd + 4

        val signature = bytes.slice(proofsStart, signatureEnd)
        val blockSize = Ints.fromByteArray(bytes.slice(signatureEnd, blockStart))
        val authDataBlock = AuthDataBlock.decode(bytes.slice(blockStart, blockStart + blockSize))

        parseProofs(
          blockStart + blockSize,
          total,
          current + 1,
          PartialProof(signature, authDataBlock.get) +: acc
        )
      } else {
        acc.reverse
      }
    }

    val targetSize = Ints.fromByteArray(bytes.take(4))
    val targetLength = 4 + targetSize
    val proofsSize = Ints.fromByteArray(bytes.slice(
      PuzLength + targetLength + PublicKeyLength + SLength, PuzLength + targetLength + PublicKeyLength + SLength + 4))

    PermaConsensusBlockField(PermaConsensusBlockData(
      BigInt(bytes.slice(4, targetLength)),
      bytes.slice(targetLength, PuzLength + targetLength),
      Ticket(
        bytes.slice(PuzLength + targetLength, PuzLength + targetLength + PublicKeyLength),
        bytes.slice(PuzLength + targetLength + PublicKeyLength, PuzLength + targetLength + PublicKeyLength + SLength),
        parseProofs(PuzLength + targetLength + PublicKeyLength + SLength + 4, proofsSize, 0, IndexedSeq.empty)
      )
    ))
  }
}
