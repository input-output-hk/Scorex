package scorex.block

import com.google.common.primitives.{Ints, Longs}
import play.api.libs.json.Json
import scorex.consensus.ConsensusModule
import scorex.crypto.encode.Base58
import scorex.transaction.account.PubKey25519Account
import scorex.transaction.state.StateElement
import scorex.transaction.{Signable, Transaction, TransactionModule}
import scorex.utils.ScorexLogging

import scala.util.{Failure, Try}

/**
  * A block is an atomic piece of data network participates are agreed on.
  *
  * A block has:
  * - transactions data: a sequence of transactions, where a transaction is an atomic state update.
  * Some metadata is possible as well(transactions Merkle tree root, state Merkle tree root etc).
  *
  * - consensus data to check whether block was generated by a right party in a right way. E.g.
  * "baseTarget" & "generatorSignature" fields in the Nxt block structure, nonce & difficulty in the
  * Bitcoin block structure.
  *
  * - a signature(s) of a block generator(s)
  *
  * - additional data: block structure version no, timestamp etc
  */

trait Block[SE <: StateElement, TX <: Transaction[SE]] extends Signable with ScorexLogging {
  type ConsensusDataType
  type TransactionDataType

  implicit val consensusModule: ConsensusModule[ConsensusDataType, SE, TX]
  implicit val transactionModule: TransactionModule[TransactionDataType, SE, TX]

  val consensusDataField: BlockField[ConsensusDataType]
  val transactionDataField: BlockField[TransactionDataType]

  val versionField: ByteBlockField
  val timestampField: LongBlockField
  val referenceField: BlockIdField
  val producerField: ProducerBlockField[SE]
  val signatureField: SignatureBlockField

  // Some block characteristic which is uniq for a block
  // e.g. hash or signature. Used in referencing
  val uniqueId = signatureField.bytes

  lazy val encodedId: String = Base58.encode(uniqueId)

  lazy val transactions: Seq[TX] = transactionModule.transactions(this)

  lazy val fee = consensusModule.feesDistribution(this).values.sum

  lazy val json =
    versionField.json ++
      timestampField.json ++
      referenceField.json ++
      consensusDataField.json ++
      transactionDataField.json ++
      producerField.json ++
      signatureField.json ++
      Json.obj(
        "fee" -> fee,
        "blocksize" -> bytes.length
      )

  override lazy val messageToSign = {
    val txBytesSize = transactionDataField.bytes.length
    val txBytes = Ints.toByteArray(txBytesSize) ++ transactionDataField.bytes

    val cBytesSize = consensusDataField.bytes.length
    val cBytes = Ints.toByteArray(cBytesSize) ++ consensusDataField.bytes

    versionField.bytes ++
      timestampField.bytes ++
      referenceField.bytes ++
      cBytes ++
      txBytes
  }

  lazy val producer = producerField.value

  lazy val signature = signatureField.value

  //todo: handle the nonce
  lazy val nonce: Array[Byte] = Array()


  def isValid: Boolean = {
    if (transactionModule.blockStorage.history.contains(this)) true //applied blocks are valid
    else {
      lazy val history = transactionModule.blockStorage.history.contains(referenceField.value)
      lazy val sigValid = producer.verify(messageToSign, signature, nonce)
      lazy val consensus = consensusModule.isValid(this)
      lazy val transaction = transactionModule.isValid(this)

      if (!history) log.debug(s"Invalid block $encodedId: no parent block in history")
      else if (!sigValid) log.debug(s"Invalid block $encodedId: signature is not valid")
      else if (!consensus) log.debug(s"Invalid block $encodedId: consensus data is not valid")
      else if (!transaction) log.debug(s"Invalid block $encodedId: transaction data is not valid")

      history && sigValid && consensus && transaction
    }
  }

  override def equals(obj: scala.Any): Boolean = obj match {
    case b: Block[SE, TX] => b.uniqueId.sameElements(this.uniqueId)
    case _ => false
  }
}


object Block extends ScorexLogging {
  type BlockId = Array[Byte]

  //TODO BytesParseable[Block] ??
  def parseBytes[CDT, TDT, SE <: StateElement, TX <: Transaction[SE]](bytes: Array[Byte])
                                                                     (implicit consModule: ConsensusModule[CDT, SE, TX],
                                                                      transModule: TransactionModule[TDT, SE, TX]): Try[Block[SE, TX]] = Try {

    val version = bytes.head

    var position = 1

    val timestamp = Longs.fromByteArray(bytes.slice(position, position + 8))
    position += 8

    val reference = bytes.slice(position, position + Block.BlockIdLength)
    position += BlockIdLength

    val cBytesLength = Ints.fromByteArray(bytes.slice(position, position + 4))
    position += 4
    val cBytes = bytes.slice(position, position + cBytesLength)
    val consBlockField = consModule.parseBytes(cBytes).get
    position += cBytesLength

    val tBytesLength = Ints.fromByteArray(bytes.slice(position, position + 4))
    position += 4
    val tBytes = bytes.slice(position, position + tBytesLength)
    val txBlockField = transModule.parseBytes(tBytes).get
    position += tBytesLength

    val genPK = bytes.slice(position, position + EllipticCurveImpl.KeyLength)
    position += EllipticCurveImpl.KeyLength

    val signature = bytes.slice(position, position + EllipticCurveImpl.SignatureLength)

    new Block[SE, TX] {
      override type ConsensusDataType = CDT
      override type TransactionDataType = TDT

      override val transactionDataField: BlockField[TransactionDataType] = txBlockField

      override implicit val consensusModule: ConsensusModule[ConsensusDataType, _, TX] = consModule
      override implicit val transactionModule: TransactionModule[TransactionDataType, SE, TX] = transModule

      override val versionField: ByteBlockField = ByteBlockField("version", version)
      override val referenceField: BlockIdField = BlockIdField("reference", reference)

      override val signerDataField: SignerDataBlockField =
        SignerDataBlockField("signature", SignerData(new PubKey25519Account(genPK), signature))

      override val consensusDataField: BlockField[ConsensusDataType] = consBlockField

      override val uniqueId: BlockId = signature

      override val timestampField: LongBlockField = LongBlockField("timestamp", timestamp)
    }
  }.recoverWith { case t: Throwable =>
    log.error("Error when parsing block", t)
    t.printStackTrace()
    Failure(t)
  }

  def build[CDT, TT, TDT, SE <: StateElement, TX <: Transaction[SE]](version: Byte,
                                                                     timestamp: Long,
                                                                     reference: BlockId,
                                                                     consensusData: CDT,
                                                                     transactionData: TDT,
                                                                     producer: SE,
                                                                     signature: Array[Byte])
                                                                    (implicit consModule: ConsensusModule[CDT, SE, TX],
                                                                     transModule: TransactionModule[TDT, SE, TX]): Block[SE, TX] = {
    new Block[SE, TX] {
      override type ConsensusDataType = CDT
      override type TransactionDataType = TDT

      override implicit val transactionModule: TransactionModule[TDT, SE, TX] = transModule
      override implicit val consensusModule: ConsensusModule[CDT, SE, TX] = consModule

      override val versionField: ByteBlockField = ByteBlockField("version", version)

      override val transactionDataField: BlockField[TDT] = transModule.formBlockData(transactionData)

      override val referenceField: BlockIdField = BlockIdField("reference", reference)
      override val signerDataField: SignerDataBlockField = SignerDataBlockField("signature", SignerData(generator, signature))
      override val consensusDataField: BlockField[CDT] = consensusModule.formBlockData(consensusData)

      override val uniqueId: BlockId = signature

      override val timestampField: LongBlockField = LongBlockField("timestamp", timestamp)
    }
  }

  def buildAndSign[CDT, TDT, SE <: StateElement, TX <: Transaction[SE]](version: Byte,
                                                                        timestamp: Long,
                                                                        reference: BlockId,
                                                                        consensusData: CDT,
                                                                        transactionData: TDT,
                                                                        signer: SE)
                                                                       (implicit consModule: ConsensusModule[CDT, SE, TX],
                                                                        transModule: TransactionModule[TDT, SE, TX]): Block[SE, TX] = {
    val nonSignedBlock = build(version, timestamp, reference, consensusData, transactionData, signer, Array())
    val toSign = nonSignedBlock.bytes
    val signature = EllipticCurveImpl.sign(signer, toSign)
    build(version, timestamp, reference, consensusData, transactionData, signer, signature)
  }

  def genesis[CDT, TDT, SE <: StateElement, TX <: Transaction[SE]](timestamp: Long = 0L)(implicit consModule: ConsensusModule[CDT, SE, TX],
                                                                                         transModule: TransactionModule[TDT, SE, TX]): Block[SE, TX] = new Block[SE, TX] {
    override type ConsensusDataType = CDT
    override type TransactionDataType = TDT

    override implicit val transactionModule: TransactionModule[TDT, SE, TX] = transModule
    override implicit val consensusModule: ConsensusModule[CDT, SE, TX] = consModule

    override val versionField: ByteBlockField = ByteBlockField("version", 1)
    override val transactionDataField: BlockField[TDT] = transactionModule.genesisData
    override val referenceField: BlockIdField = BlockIdField("reference", Array.fill(BlockIdLength)(0: Byte))
    override val consensusDataField: BlockField[CDT] = consensusModule.genesisData
    override val uniqueId: BlockId = Array.fill(BlockIdLength)(0: Byte)

    override val timestampField: LongBlockField = LongBlockField("timestamp", timestamp)

    override val signerDataField: SignerDataBlockField = new SignerDataBlockField("signature",
      SignerData(new PubKey25519Account(Array.fill(32)(0)), Array.fill(EllipticCurveImpl.SignatureLength)(0)))
  }
}
