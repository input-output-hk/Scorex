package scorex.consensus.qora.api.http

import javax.ws.rs.Path

import akka.actor.ActorRefFactory
import akka.http.scaladsl.server.Route
import io.swagger.annotations._
import scorex.api.http.{ApiError, ApiRoute, CommonApiFunctions}
import scorex.app.Application
import scorex.consensus.qora.QoraLikeConsensusModule

import scala.util.Try
import io.circe.generic.auto._
import io.circe.syntax._

@Path("/consensus")
@Api(value = "/consensus", description = "Consensus-related calls")
case class QoraConsensusApiRoute(override val application: Application)
                                (implicit val context: ActorRefFactory)
  extends ApiRoute with CommonApiFunctions {

  private val consensusModule = application.consensusModule.asInstanceOf[QoraLikeConsensusModule[_, _]]

  override val route: Route =
    pathPrefix("consensus") {
      algo ~ time ~ timeForBalance ~ nextGenerating ~ generating
    }

  @Path("/generatingbalance/{blockId}")
  @ApiOperation(value = "Generating balance", notes = "Generating balance of a block with given id", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "blockId", value = "Block id", required = true, dataType = "String", paramType = "path")
  ))
  def generating: Route = {
    path("generatingbalance" / Segment) { case encodedSignature =>
      getJsonRoute {
        withBlock(consensusModule, encodedSignature) { block =>
          ("generatingbalance" -> block.generatingBalance).asJson
        }
      }
    }
  }

  @Path("/generatingbalance")
  @ApiOperation(value = "Next generating balance", notes = "Generating balance of a next block", httpMethod = "GET")
  def nextGenerating: Route = {
    path("generatingbalance") {
      getJsonRoute {
        val generatingBalance = consensusModule.getNextBlockGeneratingBalance()
        ("generatingbalance" -> generatingBalance).asJson
      }
    }
  }

  @Path("/time/{balance}")
  @ApiOperation(value = "Balance time", notes = "estimated time before next block with given generating balance", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "balance", value = "Generating balance", required = true, dataType = "Long", paramType = "path")
  ))
  def timeForBalance: Route = {
    path("time" / Segment) { case generatingBalance =>
      getJsonRoute {
        Try {
          val timePerBlock = consensusModule.getBlockTime(generatingBalance.toLong)
          ("time" -> timePerBlock).asJson
        }.getOrElse(ApiError.invalidNotNumber)
      }
    }
  }

  @Path("/time")
  @ApiOperation(value = "Time", notes = "Estimated time before next block", httpMethod = "GET")
  def time: Route = {
    path("time") {
      getJsonRoute {
        val block = consensusModule.lastBlock
        val genBalance = block.generatingBalance
        val timePerBlock = consensusModule.getBlockTime(genBalance)
        ("time" -> timePerBlock).asJson
      }
    }
  }

  @Path("/algo")
  @ApiOperation(value = "Consensus algo", notes = "Shows which consensus algo being using", httpMethod = "GET")
  def algo: Route = {
    path("algo") {
      getJsonRoute {
        ("consensusAlgo" -> "qora").asJson
      }
    }
  }
}