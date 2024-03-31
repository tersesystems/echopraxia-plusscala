package com.tersesystems.echopraxia.plusscala.api

import java.time.Instant

// This trait should be extended for domain model classes
trait Logging extends LoggingBase {
  implicit val instantToValue: ToValue[Instant] = instant => ToValue(instant.toString)
}
