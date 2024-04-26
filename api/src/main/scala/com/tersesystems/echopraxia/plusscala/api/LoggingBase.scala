package com.tersesystems.echopraxia.plusscala.api

/**
 * This is a trait that should be extended by domain logging traits.
 *
 * {{{
 *  trait Logging extends LoggingBase with StringToNameImplicits {
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
trait LoggingBase
    extends ValueTypeClasses
    with NameTypeClass
    with FieldTypeClass
    with OptionToValueImplicits
    with EitherToValueImplicits
    with FutureToValueImplicits
    with FieldConversionImplicits
