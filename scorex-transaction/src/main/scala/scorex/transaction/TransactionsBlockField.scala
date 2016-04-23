package scorex.transaction

import com.google.common.primitives.{Ints, Bytes}
import play.api.libs.json.{Json, JsObject}
import scorex.block.BlockField
import scorex.transaction.account.AccountTransaction

case class TransactionsBlockField(override val value: Seq[AccountTransaction])
  extends BlockField[Seq[AccountTransaction]] {

  import SimpleTransactionModule.MaxTransactionsPerBlock

  override val name = "transactions"

  override lazy val json: JsObject = Json.obj(name -> Json.arr(value.map(_.json)))

  override lazy val bytes: Array[Byte] = {
    val txCount = value.size.ensuring(_ <= MaxTransactionsPerBlock).toByte
    value.foldLeft(Array(txCount)) { case (bs, tx) =>
      val txBytes = tx.bytes
      bs ++ Ints.toByteArray(txBytes.length) ++ txBytes
    }
  }
}
