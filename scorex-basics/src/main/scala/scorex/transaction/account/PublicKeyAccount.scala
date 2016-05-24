package scorex.transaction.account

abstract class PublicKeyAccount(val publicKey: Array[Byte])
  extends Account(Account.fromPublicKey(publicKey)) with Serializable
