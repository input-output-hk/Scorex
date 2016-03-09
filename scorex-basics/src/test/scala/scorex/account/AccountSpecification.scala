package scorex.account

import org.scalatest.prop.{GeneratorDrivenPropertyChecks, PropertyChecks}
import org.scalatest.{Matchers, PropSpec}
import scorex.transaction.account.Account

class AccountSpecification extends PropSpec with PropertyChecks with GeneratorDrivenPropertyChecks with Matchers {

  property("Account.fromPublicKey should generate valid account") {
    forAll { data: Array[Byte] =>
      Account.isValidAddress(Account.fromPublicKey(data)) shouldBe true
    }
  }

}
