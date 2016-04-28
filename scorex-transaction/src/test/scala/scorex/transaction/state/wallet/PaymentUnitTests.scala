package scorex.transaction.state.wallet

import org.scalatest._
import play.api.libs.json.Json

class PaymentUnitTests extends FunSuite {

  test("Parsing payment json without sender field returns JsError") {
    val body = "{\"amount\": 100,\n\"fee\": 1,\n\"recipient\": \"recipient\"}"
    val jsValue = Json.parse(body);
    assert(jsValue.validate[Payment].isError)
  }
}
