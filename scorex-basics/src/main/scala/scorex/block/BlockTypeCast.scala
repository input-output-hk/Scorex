package scorex.block

import scorex.transaction.Transaction
import shapeless._


object BlockTypeCast {

  implicit def blockTypeable[TX <: Transaction[_]]: Typeable[Block[TX]] =
    new Typeable[Block[TX]] {

      def cast(t: Any): Option[Block[TX]] = {
        if (t == null) None
        else if (t.isInstanceOf[Block[_]]) {
          //todo: potentially dangerous, but it is not clear atm how to distinguish a type from block representation
          Some(t.asInstanceOf[Block[TX]])
        } else None
      }

      override def describe: String = "Block[SE]"
    }

}
