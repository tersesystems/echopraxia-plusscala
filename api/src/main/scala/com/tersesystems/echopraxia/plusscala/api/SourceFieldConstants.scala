package com.tersesystems.echopraxia.plusscala.api

import java.util.ResourceBundle

object SourceFieldConstants {

  private val bundle = ResourceBundle.getBundle("echopraxia/sourcefields");

  val sourcecode: String = bundle.getString("sourcecode")
  val file: String       = bundle.getString("sourcecode.file")
  val line: String       = bundle.getString("sourcecode.line")
  val enclosing: String  = bundle.getString("sourcecode.enclosing")

}
