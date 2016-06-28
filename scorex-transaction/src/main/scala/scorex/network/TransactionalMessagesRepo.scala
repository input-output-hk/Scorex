package scorex.network

import scorex.network.message.Message.MessageCode
import scorex.network.message.MessageSpec
import scorex.transaction.LagonakiTransaction

import scala.util.Try

object TransactionalMessagesRepo {

  object TransactionMessageSpec extends MessageSpec[LagonakiTransaction] {
    override val messageCode: MessageCode = 25: Byte

    override val messageName: String = "Transaction message"

    override def deserializeData(bytes: Array[MessageCode]): Try[LagonakiTransaction] =
      LagonakiTransaction.parseBytes(bytes)

    override def serializeData(tx: LagonakiTransaction): Array[Byte] = tx.bytes
  }

  val specs = Seq(TransactionMessageSpec)
}
