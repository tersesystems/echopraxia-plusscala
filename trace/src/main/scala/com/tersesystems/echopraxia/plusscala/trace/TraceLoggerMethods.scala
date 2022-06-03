package com.tersesystems.echopraxia.plusscala.trace

import com.tersesystems.echopraxia.plusscala.api.{Condition, DefaultMethodsSupport}
import sourcecode.{Args, Enclosing, File, FullName, Line}

trait TraceLoggerMethods[FB <: TracingFieldBuilder] { self: DefaultMethodsSupport[FB] =>

  // Need a solid value to use dependent types here
  private[trace] val fb: FB = this.fieldBuilder

  type ToValue[B] = this.fb.ToValue[B]

  // -----------------------------------------
  // Trace

  def trace[B: ToValue](attempt: => B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B

  def trace[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B

  // -----------------------------------------
  // Debug

  def debug[B: ToValue](attempt: => B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B

  def debug[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B

  // -----------------------------------------
  // Info

  def info[B: ToValue](attempt: => B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B

  def info[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B

  // -----------------------------------------
  // Warn

  def warn[B: ToValue](attempt: => B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B

  def warn[B: ToValue](condition: Condition)(attempt: => B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B

  // -----------------------------------------
  // Error

  def error[B: ToValue](attempt: => B)(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B

  def error[B: ToValue](condition: Condition)(
      attempt: => B
  )(implicit line: Line, file: File, fullname: FullName, enc: Enclosing, args: Args): B
}