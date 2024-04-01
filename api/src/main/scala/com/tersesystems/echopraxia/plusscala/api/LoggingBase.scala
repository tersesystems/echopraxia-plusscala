package com.tersesystems.echopraxia.plusscala.api

/**
 * This is a trait that should be extended by domain logging traits.
 *
 * {{{
 *  trait Logging extends LoggingBase {
 *    implicit val currencyToField: ToField[Currency] = ToField(_ => "currency", currency => ToValue(currency.getCurrencyCode))
 *
 *    implicit val priceToField: ToField[Price] = ToField(_ => "price", price => ToObjectValue(price.currency, "amount" -> price.amount))
 *
 *    // Renders price value as $8.95 in line oriented PatternLayout apppenders
 *    implicit val priceToStringValue: ToStringFormat[Price] = (price: Price) => Value.string(price.toString)
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
    with FieldConversionImplicits
    with OptionValueTypes
    with EitherValueTypes
    with FutureValueTypes
    with ToFieldTypes
    with ThrowableToName
