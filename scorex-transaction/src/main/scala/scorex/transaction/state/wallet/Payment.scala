package scorex.transaction.state.wallet

case class Payment(amount: Long, fee: Long, sender: String, recipient: String)