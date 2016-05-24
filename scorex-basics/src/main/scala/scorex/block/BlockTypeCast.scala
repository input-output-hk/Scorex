package scorex.block

import scorex.transaction.Transaction
import scorex.transaction.state.StateElement
import shapeless._


object BlockTypeCast {

  implicit def blockTypeable[SE <: StateElement, TX <: Transaction[SE]]: Typeable[Block[SE, TX]] =
    new Typeable[Block[SE, TX]] {

      def cast(t: Any): Option[Block[SE, TX]] = Option(t).flatMap {
        _ match {
          case _: Block[_, _] =>
            //todo: potentially dangerous, but it is not clear atm how to distinguish a type from block representation
            Some(t.asInstanceOf[Block[SE, TX]])

          case _ => None
        }
      }

      override def describe: String = "Block[SE]"
    }

}
