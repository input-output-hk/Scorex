package scorex.api.http

import io.circe.Json

object SimpleTransactionalModuleErrors {
  import ApiError.encodeToJson

  val walletNotExist: Json = ApiError(201, "wallet does not exist")

  val walletAddressNotExists: Json = ApiError(202, "address does not exist in wallet")

  val walletLocked: Json = ApiError(203, "wallet is locked")

  val walletAlreadyExists: Json = ApiError(204, "wallet already exists")

  val walletSeedExportFailed: Json = ApiError(205, "seed exporting failed")


  val transactionNotExists: Json = ApiError(311, "transactions does not exist")

  val noBalance: Json = ApiError(2, "not enough balance")

  val negativeAmount: Json = ApiError(111, "negative amount")

  val negativeFee: Json = ApiError(112, "negative fee")
}