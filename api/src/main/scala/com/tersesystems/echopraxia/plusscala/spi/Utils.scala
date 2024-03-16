package com.tersesystems.echopraxia.plusscala.spi

import com.tersesystems.echopraxia.plusscala.api.ToValueAttribute

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.spi.FieldCreator
import com.tersesystems.echopraxia.spi.EchopraxiaService
import com.tersesystems.echopraxia.api.PresentationField
import com.tersesystems.echopraxia.api.Value
import com.tersesystems.echopraxia.api.Attributes

object Utils {
  @inline
  private def fieldCreator[F <: Field](clazz: Class[F]): FieldCreator[F] = EchopraxiaService.getInstance.getFieldCreator(clazz)

  private val PresentationFieldCreator = fieldCreator(classOf[PresentationField])

  private def defaultFieldCreator: FieldCreator[PresentationField] = PresentationFieldCreator

  def newField[TV](name: String, value: Value[_])(implicit ev: ToValueAttribute[TV]): PresentationField =
    defaultFieldCreator.create(name, value, ev.toAttributes(value))

  def newField(name: String, value: Value[_], attributes: Attributes): PresentationField = defaultFieldCreator.create(name, value, attributes)

}
