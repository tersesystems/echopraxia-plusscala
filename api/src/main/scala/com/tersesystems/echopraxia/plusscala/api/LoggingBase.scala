package com.tersesystems.echopraxia.plusscala.api

/**
 * This is a trait that should be extended by domain logging traits.
 *
 * This trait recognizes strings as meaningful names. If this is not what you want (for example you want enums or refined types), you can create your
 * own trait extending `LoggingTypeClasses with LoggingToValueImplicits` with your own `ToName` implicits.
 *
 * {{{
 *  trait Logging extends LoggingBase {
 *    implicit val currencyToField: ToField[Currency] = ToField(_ => "currency", currency => ToValue(currency.getCurrencyCode))
 *
 *    implicit val priceToField: ToField[Price] = ToField(_ => "price", price => ToObjectValue(price.currency, "amount" -> price.amount))
 *  }
 *
 *  case class Price(amount: BigDecimal, currency: Currency) {
 *    override def toString: String = {
 *      val numberFormat = NumberFormat.getCurrencyInstance
 *      numberFormat.setCurrency(currency)
 *      numberFormat.format(amount)
 *    }
 * }
 *
 * class MyClass extends Logging {
 *   private val logger = LoggerFactory.getLogger(getClass())
 *
 *   val price = Price(amount = 8.95, currency = Currency.getInstance("USD"))
 *   logger.info(price)
 * }
 * }}}
 */
trait LoggingBase extends LoggingTypeClasses with LoggingToValueImplicits with StringToNameImplicits

/**
 * This trait aggregates all the type classes together for convenience.
 */
trait LoggingTypeClasses extends ValueTypeClasses with NameTypeClass with FieldTypeClass

/**
 * This trait aggregates ToValue and field conversion implicits for using in field conversion for convenience.
 */
trait LoggingToValueImplicits extends FieldConversionImplicits with OptionToValueImplicits with EitherToValueImplicits with FutureToValueImplicits {
  this: ValueTypeClasses with NameTypeClass with FieldTypeClass =>
}
