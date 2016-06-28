package scorex.consensus.nxt.api.http

import javax.ws.rs.Path

import akka.actor.ActorRefFactory
import akka.http.scaladsl.server.Route
import io.swagger.annotations._
import scorex.api.http.{ApiRoute, CommonApiFunctions}
import scorex.app.Application
import scorex.consensus.nxt.NxtLikeConsensusModule
import scorex.crypto.encode.Base58

import io.circe.generic.auto._
import io.circe.syntax._

@Path("/consensus")
@Api(value = "/consensus", description = "Consensus-related calls")
class NxtConsensusApiRoute(override val application: Application)(implicit val context: ActorRefFactory)
  extends ApiRoute with CommonApiFunctions {

  //todo: asInstanceOf
  private val consensusModule = application.consensusModule.asInstanceOf[NxtLikeConsensusModule[_]]

  override val route: Route =
    pathPrefix("consensus") {
      algo ~ basetarget ~ baseTargetId ~ generationSignature ~ generationSignatureId
    }

  @Path("/generationsignature/{blockId}")
  @ApiOperation(value = "Generation signature", notes = "Generation signature of a block with specified id", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "blockId", value = "Block id ", required = true, dataType = "String", paramType = "path")
  ))
  def generationSignatureId: Route = {
    path("generationsignature" / Segment) { case encodedSignature =>
      getJsonRoute {
        withBlock(consensusModule, encodedSignature) { block =>
          val gs = consensusModule.consensusBlockData(block).generationSignature
          ("generationSignature" -> Base58.encode(gs)).asJson
        }
      }
    }
  }

  @Path("/generationsignature")
  @ApiOperation(value = "Generation signature last", notes = "Generation signature of a last block", httpMethod = "GET")
  def generationSignature: Route = {
    path("generationsignature") {
      getJsonRoute {
        val lastBlock = consensusModule.lastBlock
        val gs = consensusModule.consensusBlockData(lastBlock).generationSignature
        ("generationSignature" -> Base58.encode(gs)).asJson
      }
    }
  }

  @Path("/basetarget/{blockId}")
  @ApiOperation(value = "Base target", notes = "base target of a block with specified id", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "blockId", value = "Block id ", required = true, dataType = "String", paramType = "path")
  ))
  def baseTargetId: Route = {
    path("basetarget" / Segment) { case encodedSignature =>
      getJsonRoute {
        withBlock(consensusModule, encodedSignature) { block =>
          ("baseTarget" -> consensusModule.consensusBlockData(block).baseTarget).asJson
        }
      }
    }
  }

  @Path("/basetarget")
  @ApiOperation(value = "Base target last", notes = "Base target of a last block", httpMethod = "GET")
  def basetarget: Route = {
    path("basetarget") {
      getJsonRoute {
        val lastBlock = consensusModule.lastBlock
        val bt = consensusModule.consensusBlockData(lastBlock).baseTarget
        ("baseTarget" -> bt).asJson
      }
    }
  }

  @Path("/algo")
  @ApiOperation(value = "Consensus algo", notes = "Shows which consensus algo being using", httpMethod = "GET")
  def algo: Route = {
    path("algo") {
      getJsonRoute {
        ("consensusAlgo" -> "nxt").asJson
      }
    }
  }
}