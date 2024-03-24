package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Attributes
import com.tersesystems.echopraxia.api.Value

// Allows custom attributes on fields through implicits
trait ToValueAttributes[-T] {
  def toValue(v: T): Value[_]
  def toAttributes(value: Value[_]): Attributes
}

trait LowPriorityToValueAttributesImplicits {
  // default low priority implicit that gets applied if nothing is found
  implicit def empty[TV]: ToValueAttributes[TV] = new ToValueAttributes[TV] {
    override def toValue(v: TV): Value[_]                  = Value.nullValue()
    override def toAttributes(value: Value[_]): Attributes = Attributes.empty()
  }
}

object ToValueAttributes extends LowPriorityToValueAttributesImplicits {
  def attributes(value: Value[_], ev: ToValueAttributes[_]): Attributes = ev.toAttributes(value)
}

trait ToValueAttributeImplicits {

  implicit def iterableValueFormat[TV: ToValueAttributes]: ToValueAttributes[Iterable[TV]] = new ToValueAttributes[Iterable[TV]]() {
    override def toValue(seq: collection.Iterable[TV]): Value[_] = {
      import scala.collection.JavaConverters._
      val list: Seq[Value[_]] = seq.map(el => implicitly[ToValueAttributes[TV]].toValue(el)).toSeq
      Value.array(list.asJava)
    }

    override def toAttributes(value: Value[_]): Attributes = implicitly[ToValueAttributes[TV]].toAttributes(value)
  }

  implicit def optionValueFormat[TV: ToValueAttributes]: ToValueAttributes[Option[TV]] = new ToValueAttributes[Option[TV]] {
    override def toValue(v: Option[TV]): Value[_] = v match {
      case Some(tv) =>
        val ev = implicitly[ToValueAttributes[TV]]
        ev.toValue(tv)
      case None => Value.nullValue()
    }

    override def toAttributes(value: Value[_]): Attributes = implicitly[ToValueAttributes[TV]].toAttributes(value)
  }

  // // XXX Is there a way to make this work more easily?
  implicit def eitherToValueAttribute[TVL: ToValueAttributes, TVR: ToValueAttributes]: ToValueAttributes[Either[TVL, TVR]] =
    new ToValueAttributes[Either[TVL, TVR]] {
      // This isn't great, but we need to know whether left or right was picked for the attributes
      // and if we have a parameter (either: Either[]) in the method signature then it doesn't
      // pick it up?
      private var optEither: Option[Either[TVL, TVR]] = None

      override def toValue(v: Either[TVL, TVR]): Value[_] = {
        this.optEither = Some(v)
        v match {
          case Left(l)  => implicitly[ToValueAttributes[TVL]].toValue(l)
          case Right(r) => implicitly[ToValueAttributes[TVR]].toValue(r)
        }
      }

      override def toAttributes(value: Value[_]): Attributes = {
        // hack hack hack hack
        optEither match {
          case Some(either) =>
            either match {
              case Left(_) =>
                val left = implicitly[ToValueAttributes[TVL]]
                left.toAttributes(value)
              case Right(_) =>
                val right = implicitly[ToValueAttributes[TVR]]
                right.toAttributes(value)
            }
          case None =>
            // should never get here
            Attributes.empty()
        }
      }
    }

}
