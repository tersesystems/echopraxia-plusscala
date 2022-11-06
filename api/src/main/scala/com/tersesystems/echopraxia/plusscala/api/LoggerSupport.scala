package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.FieldBuilderResult

trait LoggerSupport[FB, LoggerType[_ <: FB]] { self =>

  def withCondition(scalaCondition: Condition): LoggerType[FB]

  def withFields(f: FB => FieldBuilderResult): LoggerType[FB]

  def withThreadContext: LoggerType[FB]

  def withFieldBuilder[T <: FB](newBuilder: T): LoggerType[T]
}
