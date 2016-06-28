package scorex.api.http

import javax.ws.rs.Path

import akka.actor.ActorRefFactory
import akka.http.scaladsl.server.Route
import io.swagger.annotations._
import scorex.app.Application
import scorex.transaction.{Wallet25519Only, SimpleTransactionModule}
import scorex.transaction.state.wallet.Payment

import scala.util.{Failure, Success}

import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser.decode


@Path("/payment")
@Api(value = "/payment", description = "Payment operations.", position = 1)
case class PaymentApiRoute(override val application: Application)(implicit val context: ActorRefFactory)
  extends ApiRoute with CommonTransactionApiFunctions {

  // TODO asInstanceOf
  implicit lazy val transactionModule: SimpleTransactionModule[_, _] = application.transactionModule.asInstanceOf[SimpleTransactionModule[_, _]]
  lazy val wallet = application.transactionModule.wallet.asInstanceOf[Wallet25519Only] //todo: aIO

  override lazy val route = payment

  @ApiOperation(value = "Send payment",
    notes = "Send payment to another wallet",
    httpMethod = "POST",
    produces = "application/json",
    consumes = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "body",
      value = "Json with data",
      required = true,
      paramType = "body",
      dataType = "scorex.transaction.state.wallet.Payment",
      defaultValue = "{\n\t\"amount\":400,\n\t\"fee\":1,\n\t\"sender\":\"senderId\",\n\t\"recipient\":\"recipientId\"\n}"
    )
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Json with response or error")
  ))
  def payment: Route = path("payment") {
    entity(as[String]) { body =>
      withAuth {
        postJsonRoute {
          walletNotExists(wallet).getOrElse {
            decode[Payment](body).toOption match {
                case Some(payment) =>
                  val txOpt = transactionModule.createPayment(payment, wallet)
                  txOpt match {
                    case Some(tx) =>
                      tx.validate(transactionModule) match {
                        case Success(_) =>
                          tx.json

                        case Failure(e) =>
                          Map("error" -> 0.asJson, "message" -> e.getMessage.asJson).asJson
                      }
                    case None =>
                      ApiError.invalidSender
                  }

                case _ =>
                  ApiError.wrongJson
              }
          }
        }
      }
    }
  }
}