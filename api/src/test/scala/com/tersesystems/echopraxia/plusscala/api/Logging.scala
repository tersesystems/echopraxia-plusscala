package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.spi.PresentationHintAttributes

// This trait should be extended for domain model classes
trait Logging extends LoggingBase

object Logging {
  def withAttributes(seq: Attribute[_]*): Attributes = {
    import scala.collection.JavaConverters._
    Attributes.create(seq.asJava)
  }

  // Add a custom string format attribute using the passed in value
  def withStringFormat(value: Value[_]): Attribute[_] = {
    PresentationHintAttributes.withToStringFormat(new SimpleFieldVisitor() {
      override def visit(f: Field): Field = Field.keyValue(f.name(), value)
    })
  }

  def withDisplayName(name: String): Attribute[_] = PresentationHintAttributes.withDisplayName(name)

  def abbreviateAfter(after: Int): Attribute[_] = PresentationHintAttributes.abbreviateAfter(after)

  def elided: Attribute[_] = PresentationHintAttributes.asElided()

  def asValueOnly: Attribute[_] = PresentationHintAttributes.asValueOnly()

  def asCardinal: Attribute[_] = PresentationHintAttributes.asCardinal()
}

