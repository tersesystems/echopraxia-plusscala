package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Attributes
import com.tersesystems.echopraxia.plusscala.spi.Utils.newField
import sourcecode._

case class SourceCode(line: Line, file: File, enclosing: Enclosing)

object SourceCode {
  implicit val sourceCodeToName: ToName[SourceCode] = ToName.create("sourcecode")
}

trait SourceCodeImplicits { self: ValueTypeClasses =>

  implicit val sourceCodeToValue: ToValue[SourceCode] = { sc =>
    ToObjectValue(
      newField("file", ToValue(sc.file.value), Attributes.empty),
      newField("line", ToValue(sc.line.value: java.lang.Integer), Attributes.empty),
      newField("enclosing", ToValue(sc.enclosing.value), Attributes.empty)
    )
  }
}
