package scorex.network

import scorex.network.message.Message.MessageCode
import scorex.network.message.MessageSpec
import scorex.transaction.state.StateElement
import scorex.transaction.{LagonakiTransaction, Transaction}

import scala.util.Try

object TransactionalMessagesRepo {

  object TransactionMessageSpec extends MessageSpec[Transaction[_ <: StateElement]] {
    override val messageCode: MessageCode = 25: Byte

    override val messageName: String = "Transaction message"

    override def deserializeData(bytes: Array[MessageCode]): Try[Transaction[_ <: StateElement]] =
      LagonakiTransaction.parse(bytes)

    override def serializeData(tx: Transaction[_ <: StateElement]): Array[MessageCode] = tx.bytes
  }

  val specs = Seq(TransactionMessageSpec)
}
