package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Value

trait FieldTypeClasses { self: ValueTypeClasses with NameTypeClasses =>
  // Provides easier packaging for ToName and ToValue
  trait ToField[-TF] {
    def toName: ToName[TF]
    def toValue: ToValue[TF]
  }

  object ToField {
    def apply[TF](nameFunction: Option[TF] => String, valueFunction: TF => Value[_]): ToField[TF] = new ToField[TF] {
      override val toName: ToName[TF]   = t => nameFunction(t)
      override val toValue: ToValue[TF] = t => valueFunction(t)
    }
  }

  // implicit conversion from a ToField to a ToValue
  implicit def convertToFieldToValue[TL: ToField]: ToValue[TL] = implicitly[ToField[TL]].toValue

  // implicit conversion from a ToField to a ToName
  implicit def convertToFieldToName[TL: ToField]: ToName[TL] = implicitly[ToField[TL]].toName
}
