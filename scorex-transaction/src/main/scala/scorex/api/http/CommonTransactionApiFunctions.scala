package scorex.api.http

import play.api.libs.json.{JsObject, JsValue}
import scorex.transaction.TransactionModule
import scorex.transaction.box.{PublicKeyProposition, Proposition}
import scorex.transaction.state.SecretHolder
import scorex.wallet.Wallet


trait CommonTransactionApiFunctions extends CommonApiFunctions {

  protected[api] def walletExists()(implicit wallet: Wallet[_, _]): Option[JsObject] =
    if (wallet.exists()) Some(WalletAlreadyExists.json) else None

  protected[api] def withPrivateKeyAccount[TM <: TransactionModule](wallet: Wallet[_, _], address: String)
                                                                   (action: TM#SH => JsValue): JsValue =
    walletNotExists(wallet).getOrElse {
      if (!PublicKeyProposition.isValidAddress(address)) {
        InvalidAddress.json
      } else {
        wallet.privateKeyAccount(address) match {
          case None => WalletAddressNotExists.json
          case Some(account) => action(account)
        }
      }
    }

  protected[api] def walletNotExists(wallet: Wallet[_, _]): Option[JsObject] =
    if (!wallet.exists()) Some(WalletNotExist.json) else None
}
