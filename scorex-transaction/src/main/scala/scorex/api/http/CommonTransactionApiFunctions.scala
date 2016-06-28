package scorex.api.http


import io.circe.Json
import scorex.api.http.ApiError._
import scorex.api.http.SimpleTransactionalModuleErrors.{walletAddressNotExists, walletAlreadyExists, walletNotExist}
import scorex.transaction.Wallet25519Only
import scorex.transaction.box.PublicKey25519Proposition
import scorex.transaction.state.PrivateKey25519Holder
import scorex.wallet.Wallet


trait CommonTransactionApiFunctions extends CommonApiFunctions {

  protected[api] def withPrivateKeyAccount(wallet: Wallet25519Only, address: String)
                                          (action: PrivateKey25519Holder => Json): Json =
    walletNotExists(wallet).getOrElse {
      if (!PublicKey25519Proposition.validPubKey(address).isSuccess) {
        invalidAddress
      } else {
        wallet.privateKeyAccount(address) match {
          case None => walletAddressNotExists
          case Some(account) => action(account)
        }
      }
    }

  protected[api] def walletExists()(implicit wallet: Wallet[_, _, _]): Option[Json] =
    if (wallet.exists()) Some(walletAlreadyExists) else None

  protected[api] def walletNotExists(wallet: Wallet[_, _, _]): Option[Json] =
    if (!wallet.exists()) Some(walletNotExist) else None
}
