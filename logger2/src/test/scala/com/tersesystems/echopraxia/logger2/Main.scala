package com.tersesystems.echopraxia.logger2

import com.tersesystems.echopraxia.api.Level
import com.tersesystems.echopraxia.plusscala.api.Condition

import java.time.Instant

object Main {

  private val logger = LoggerFactory.getLogger(BookFieldBuilder)

  def main(args: Array[String]): Unit = {
    val refBook = Book("reference", "Nigel Rees", "Sayings of the Century", 8.95)
    logger.info("{}", _("book" -> refBook))
    logger.info("{}", _("instant" -> Instant.now()), _("book" -> refBook))

    logger.error("{}", _(new IllegalStateException()))

    logger.debug(infoOrHigherCondition, "INFO message")
  }

  val infoOrHigherCondition: Condition = Condition((level, _) => level >= Level.INFO)

  val fooCondition: Condition = Condition(_.fields.exists(_.name == "foo"))

  val infoAndFoo: Condition = infoOrHigherCondition and fooCondition

}
