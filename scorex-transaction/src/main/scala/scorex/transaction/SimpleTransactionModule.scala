package scorex.transaction

import com.google.common.primitives.Ints
import scorex.app.Application
import scorex.block.{Block, BlockField}
import scorex.network.message.Message
import scorex.network.{Broadcast, NetworkController, TransactionalMessagesRepo}
import scorex.settings.Settings
import scorex.transaction.account.PublicKey25519NoncedBox
import scorex.transaction.box.PublicKey25519Proposition
import scorex.transaction.proof.Signature25519
import scorex.transaction.state.PrivateKey25519Holder
import scorex.transaction.state.database.UnconfirmedTransactionsDatabaseImpl
import scorex.transaction.state.database.blockchain.{PersistentLagonakiState, StoredBlockTree, StoredBlockchain}
import scorex.transaction.state.wallet.Payment
import scorex.utils._
import scorex.wallet.Wallet
import shapeless.Sized
import shapeless.syntax.typeable._

import scala.concurrent.duration._
import scala.util.Try


class SimpleTransactionModule(implicit val settings: TransactionSettings with Settings, application: Application)
  extends TransactionModule with ScorexLogging {

  import SimpleTransactionModule._

  val consensusModule = application.consensusModule
  val networkController = application.networkController

  val TransactionSizeLength = 4
  val InitialBalance = 60000000000L

  private val instance = this

  override val blockStorage = new BlockStorage[LagonakiTransaction] {

    override val MaxRollback: Int = settings.MaxRollback

    override val history: History = settings.history match {
      case s: String if s.equalsIgnoreCase("blockchain") =>
        new StoredBlockchain[SimpleTransactionModule](settings.dataDirOpt)(consensusModule, instance)
      case s: String if s.equalsIgnoreCase("blocktree") =>
        new StoredBlockTree(settings.dataDirOpt, MaxRollback)(consensusModule, instance)
      case s =>
        log.error(s"Unknown history storage: $s. Use StoredBlockchain...")
        new StoredBlockchain(settings.dataDirOpt)(consensusModule, instance)
    }

    override val state = new PersistentLagonakiState(settings.dataDirOpt.map(_ + "/state.dat"))

  }

  /**
    * In Lagonaki, transaction-related data is just sequence of transactions. No Merkle-tree root of txs / state etc
    *
    * @param bytes - serialized sequence of transaction
    * @return
    */
  override def parseBytes(bytes: Array[Byte]): Try[TransactionsBlockField] = Try {
    bytes.isEmpty match {
      case true => TransactionsBlockField(Seq())
      case false =>
        val txData = bytes.tail
        val txCount = bytes.head // so 255 txs max
        formBlockData((1 to txCount).foldLeft((0: Int, Seq[LagonakiTransaction]())) { case ((pos, txs), _) =>
          val transactionLengthBytes = txData.slice(pos, pos + TransactionSizeLength)
          val transactionLength = Ints.fromByteArray(transactionLengthBytes)
          val transactionBytes = txData.slice(pos + TransactionSizeLength, pos + TransactionSizeLength + transactionLength)
          val transaction = LagonakiTransaction.parseBytes(transactionBytes).get

          (pos + TransactionSizeLength + transactionLength, txs :+ transaction)
        }._2)
    }
  }

  override def formBlockData(transactions: StoredInBlock): TransactionsBlockField = TransactionsBlockField(transactions)

  //TODO asInstanceOf
  override def transactions(block: Block): StoredInBlock =
    block.transactionDataField.asInstanceOf[TransactionsBlockField].value

  override def packUnconfirmed(): StoredInBlock =
    blockStorage.state.validate(UnconfirmedTransactionsDatabaseImpl.all().sortBy(-_.fee).take(MaxTransactionsPerBlock))
      .cast[StoredInBlock]
      .getOrElse(Seq())

  //todo: check: clear unconfirmed txs on receiving a block
  override def clearFromUnconfirmed(data: StoredInBlock): Unit = {
    data.foreach(tx => UnconfirmedTransactionsDatabaseImpl.getBySignature(tx.signature) match {
      case Some(unconfirmedTx) => UnconfirmedTransactionsDatabaseImpl.remove(unconfirmedTx)
      case None =>
    })

    val lastBlockTs = blockStorage.history.lastBlock.timestampField.value
    UnconfirmedTransactionsDatabaseImpl.all().foreach { tx =>
      if ((lastBlockTs - tx.timestamp).seconds > MaxTimeForUnconfirmed) UnconfirmedTransactionsDatabaseImpl.remove(tx)
    }

    val txs = UnconfirmedTransactionsDatabaseImpl.all()
    txs.diff(blockStorage.state.validate(txs)).foreach(tx => UnconfirmedTransactionsDatabaseImpl.remove(tx))
  }

  override def onNewOffchainTransaction(transaction: LagonakiTransaction): Unit = transaction match {
    case tx: LagonakiTransaction =>
      if (UnconfirmedTransactionsDatabaseImpl.putIfNew(tx)) {
        val spec = TransactionalMessagesRepo.TransactionMessageSpec
        val ntwMsg = Message(spec, Right(tx), None)
        networkController ! NetworkController.SendToNetwork(ntwMsg, Broadcast)
      }
    case _ => throw new Error("Wrong kind of transaction!")
  }

  def createPayment(payment: Payment, wallet: Wallet[_, _]): Option[LagonakiTransaction] = {
    wallet.privateKeyAccount(payment.sender).flatMap { sender =>
      createPayment(sender, new Account(payment.recipient), payment.amount, payment.fee)
    }
  }

  def createPayment(sender: PrivateKey25519Holder, recipient: PublicKey25519Proposition, amount: Long, fee: Long): Try[LagonakiTransaction] = Try {
    val time = NTP.correctedTime()
    val nonce = blockStorage.state.closedBox(sender.publicCommitment.id).get.asInstanceOf[PublicKey25519NoncedBox].nonce
    val payment = LagonakiTransaction(sender, recipient, nonce + 1, amount, fee, time)
    if (blockStorage.state.isValid(payment)) onNewOffchainTransaction(payment)
    payment
  }

  override def genesisData: BlockField[StoredInBlock] = {
    val ipoMembers = List(
      //peer 1 accounts
      "jACSbUoHi4eWgNu6vzAnEx583NwmUAVfS",
      "aptcN9CfZouX7apreDB6WG2cJVbkos881",
      "kVVAu6F21Ax2Ugddms4p5uXz4kdZfAp8g",
      //peer 2 accounts
      "mobNC7SHZRUXDi4GrZP9T2F4iLC1ZidmX",
      "ffUTdmFDesA7NLqLaVfUNgQRD2Xn4tNBp",
      "UR2WjoDCW32XAvYuPbyQW3guxMei5HKf1"
    )

    val timestamp = 0L
    val totalBalance = InitialBalance

    val genesisSignature = Signature25519(Array.fill(64)(0: Byte))
    val txs = ipoMembers.map { addr =>
      val recipient = PublicKey25519Proposition(Sized.wrap(Random.randomBytes(32)))
      LagonakiTransaction(LagonakiTransaction.GodAccount, recipient, 0, totalBalance / ipoMembers.length,
        0, timestamp, genesisSignature)
    }

    TransactionsBlockField(txs)
  }

  //todo: safe casting from shapeless?
  override def isValid(block: Block): Boolean = {
    block.transactions match {
      case transactions: Seq[LagonakiTransaction] =>
        blockStorage.state.areValid(transactions, blockStorage.history.heightOf(block))
      case _ => ???
    }
  }
}

object SimpleTransactionModule {
  type StoredInBlock = Seq[LagonakiTransaction]

  val MaxTimeForUnconfirmed = 1.hour
  val MaxTransactionsPerBlock = 100
}
