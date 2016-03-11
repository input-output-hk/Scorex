package scorex.perma.consensus

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, Writes}
import scorex.crypto.signatures.SigningFunctions.Signature
import scorex.crypto.ads.merkle.AuthDataBlock
import scorex.perma.settings.PermaConstants._
import scorex.utils.JsonSerialization

case class PartialProof(signature: Signature, segment: AuthDataBlock[DataSegment])

object PartialProof extends JsonSerialization {
  implicit val writes: Writes[PartialProof] = (
    (JsPath \ "signature").write[Bytes] and
      (JsPath \ "segment").write[AuthDataBlock[DataSegment]]
    ) (unlift(PartialProof.unapply))

  implicit val reads: Reads[PartialProof] = (
    (JsPath \ "signature").read[Bytes] and
      (JsPath \ "segment").read[AuthDataBlock[DataSegment]]
    ) (PartialProof.apply _)
}
