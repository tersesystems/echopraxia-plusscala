package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Value

trait ToLogTypes { self: ValueTypeClasses =>
  // Provides easier packaging for ToName and ToValue
  trait ToLog[-TF] {
    def toName: ToName[TF]
    def toValue: ToValue[TF]
  }

  object ToLog {
    def create[TF](name: String, valueFunction: TF => Value[_]): ToLog[TF] = new ToLog[TF] {
      override val toName: ToName[TF]   = ToName.create(name)
      override val toValue: ToValue[TF] = t => valueFunction(t)
    }
  }

  // implicit conversion from a ToLog to a ToValue
  implicit def convertToLogToValue[TL: ToLog]: ToValue[TL] = implicitly[ToLog[TL]].toValue

  // implicit conversion from a ToLog to a ToName
  implicit def convertToLogToName[TL: ToLog]: ToName[TL] = implicitly[ToLog[TL]].toName
}
