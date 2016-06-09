package scorex.block

import scorex.transaction.Transaction
import shapeless._


object BlockTypeCast {

  implicit def blockTypeable[TX <: Transaction]: Typeable[Block[TX]] =
    new Typeable[Block[TX]] {

      def cast(t: Any): Option[Block[TX]] = Option(t).flatMap {
        _ match {
          case _: Block[_] =>
            //todo: potentially dangerous, but it is not clear atm how to distinguish a type from block representation
            Some(t.asInstanceOf[Block[TX]])

          case _ => None
        }
      }

      override def describe: String = "Block[TX]"
    }
}