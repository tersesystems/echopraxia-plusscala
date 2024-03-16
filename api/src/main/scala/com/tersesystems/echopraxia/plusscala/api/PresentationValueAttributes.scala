package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.Attributes
import com.tersesystems.echopraxia.api.Value
import com.tersesystems.echopraxia.api.Attribute
import com.tersesystems.echopraxia.spi.PresentationHintAttributes
import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.api.SimpleFieldVisitor

trait ToStringFormat[-T] extends ToValueAttribute[T] {
  override def toAttributes(value: Value[_]): Attributes = Attributes.create(ToStringFormat.withToStringFormat(value))
}

object ToStringFormat {
  // Add a custom string format attribute using the passed in value
  def withToStringFormat(value: Value[_]): Attribute[_] = {
    PresentationHintAttributes.withToStringFormat(new SimpleFieldVisitor() {
      override def visit(f: Field): Field = Field.keyValue(f.name(), value)
    })
  }
}

trait WithDisplayName[-T] extends ToValueAttribute[T] {
  def displayName: String
  override def toAttributes(value: Value[_]): Attributes = Attributes.create(WithDisplayName(displayName))
}

object WithDisplayName {
  def apply(name: String): Attribute[_] = PresentationHintAttributes.withDisplayName(name)
}

trait AbbreviateAfter[-T] extends ToValueAttribute[T] {
  def after: Int
  override def toAttributes(value: Value[_]): Attributes = Attributes.create(AbbreviateAfter(after))
}

object AbbreviateAfter {
  def apply(after: Int): Attribute[_] = PresentationHintAttributes.abbreviateAfter(after)
}

trait Elided[-T] extends ToValueAttribute[T] {
  override def toAttributes(value: Value[_]): Attributes = Elided.attributes
}

object Elided {
  val attributes = Attributes.create(apply())

  def apply() = PresentationHintAttributes.asElided()
}

trait AsValueOnly[-T] extends ToValueAttribute[T] {
  override def toAttributes(value: Value[_]): Attributes = AsValueOnly.attributes
}

object AsValueOnly {
  val attributes = Attributes.create(apply())

  def apply() = PresentationHintAttributes.asElided()
}

trait AsCardinal[-T] extends ToValueAttribute[T] {
  override def toAttributes(value: Value[_]): Attributes = AsCardinal.attributes
}

object AsCardinal {
  val attributes = Attributes.create(apply())

  def apply() = PresentationHintAttributes.asCardinal()
}
