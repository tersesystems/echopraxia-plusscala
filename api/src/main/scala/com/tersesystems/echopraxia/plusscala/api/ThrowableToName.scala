package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.spi.FieldConstants

trait ThrowableToName {

  // All exceptions should use "exception" field constant by default
  implicit def throwableToName[T <: Throwable]: ToName[T] = ToName.create(FieldConstants.EXCEPTION)
}

object ThrowableToName extends ThrowableToName