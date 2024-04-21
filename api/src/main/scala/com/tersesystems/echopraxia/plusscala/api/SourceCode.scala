package com.tersesystems.echopraxia.plusscala.api

import sourcecode._

import java.util.ResourceBundle

final case class SourceCode(line: Line, file: File, enclosing: Enclosing)

object SourceCode {
  private val bundle = ResourceBundle.getBundle("echopraxia/sourcecode")

  val SourceCode: String = bundle.getString("sourcecode")
  val File: String       = bundle.getString("file")
  val Line: String       = bundle.getString("line")
  val Enclosing: String  = bundle.getString("enclosing")
}
