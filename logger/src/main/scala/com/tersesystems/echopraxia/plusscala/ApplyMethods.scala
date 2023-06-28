package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.FieldBuilderResult
import com.tersesystems.echopraxia.plusscala.api.Condition

trait ApplyMethods[FB] {

  def apply(message: String)
  def apply(condition: Condition, message: String)

  def apply(message: String, f: FB => FieldBuilderResult)
  def apply(condition: Condition, message: String, f: FB => FieldBuilderResult)
}
