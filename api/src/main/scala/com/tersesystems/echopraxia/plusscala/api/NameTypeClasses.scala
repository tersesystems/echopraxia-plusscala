package com.tersesystems.echopraxia.plusscala.api

import echopraxia.api.FieldConstants

import scala.annotation.implicitNotFound
import scala.util.Failure
import scala.util.Success
import scala.util.Try

/**
 * Add this trait to get access to the ToName type class.
 */
trait NameTypeClasses {
  // this needs to be a dependent type because implicit type resolution only works on a
  // field builder if it's dependent to the type itself.

  @implicitNotFound("Could not find an implicit ToName[${T}]")
  trait ToName[-T] {
    def toName(t: Option[T]): String
  }

  object ToName {
    implicit def throwableToName[T <: Throwable]: ToName[T] = _ => FieldConstants.EXCEPTION

    implicit val sourceCodeToName: ToName[SourceCode] = _ => SourceCode.SourceCode

    def apply[T: ToName](t: T): String = implicitly[ToName[T]].toName(Option(t))
  }
}

trait StringToNameImplicits { this: NameTypeClasses =>
  implicit val stringToName: ToName[String] = _.orNull
}

trait OptionToNameImplicits { this: NameTypeClasses =>
  implicit def optionToName[T: ToName]: ToName[Option[T]] = (t: Option[Option[T]]) => implicitly[ToName[T]].toName(t.flatten)
}

trait EitherToNameImplicits { this: NameTypeClasses =>
  implicit def eitherToName[L: ToName, R: ToName]: ToName[Either[L, R]] = {
    case Some(either) =>
      either match {
        case Left(l)  => ToName(l)
        case Right(r) => ToName(r)
      }
    case None => null
  }
}

trait TryToNameImplicits { this: NameTypeClasses =>
  implicit def tryToName[T: ToName]: ToName[Try[T]] = {
    case Some(t) =>
      t match {
        case Success(v) => ToName(v)
        case Failure(e) => ToName(e)
      }
    case None => implicitly[ToName[T]].toName(None)
  }
}
