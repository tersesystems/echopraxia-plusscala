package com.tersesystems.echopraxia.plusscala.api

import java.time.Instant

// This trait should be extended for domain model classes
trait Logging extends LoggingBase with OptionToNameImplicits with TryToNameImplicits with EitherToNameImplicits {
  implicit val instantToValue: ToValue[Instant] = v => ToValue(v.toString)
}
