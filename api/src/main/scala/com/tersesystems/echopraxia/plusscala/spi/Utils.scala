package com.tersesystems.echopraxia.plusscala.spi

import com.tersesystems.echopraxia.api.Attributes
import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.api.PresentationField
import com.tersesystems.echopraxia.api.Value
import com.tersesystems.echopraxia.spi.EchopraxiaService
import com.tersesystems.echopraxia.spi.FieldCreator

object Utils {
  @inline
  private def fieldCreator[F <: Field](clazz: Class[F]): FieldCreator[F] = EchopraxiaService.getInstance.getFieldCreator(clazz)

  private val PresentationFieldCreator = fieldCreator(classOf[PresentationField])

  private def defaultFieldCreator: FieldCreator[PresentationField] = PresentationFieldCreator

  def newField(name: String, value: Value[_], attributes: Attributes): PresentationField = defaultFieldCreator.create(name, value, attributes)

}
