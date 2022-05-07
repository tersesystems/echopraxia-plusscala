package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.FieldBuilderResult

trait LoggerSupport[FB] {

  def withCondition(scalaCondition: Condition): LoggerSupport[FB]

  def withFields(f: FB => FieldBuilderResult): LoggerSupport[FB]

  def withThreadContext: LoggerSupport[FB]

  def withFieldBuilder[T <: FB](newBuilder: T): LoggerSupport[T]
}
