package echopraxia.plusscala.api

import java.time.Instant

// This trait should be extended for domain model classes
trait Logging extends EchopraxiaBase {
  implicit val instantToValue: ToValue[Instant] = v => ToValue(v.toString)
}
