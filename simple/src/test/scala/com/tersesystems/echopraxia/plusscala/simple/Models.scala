package com.tersesystems.echopraxia.plusscala.simple

import java.util.Currency

case class Category(raw: String) extends AnyVal
case class Author(raw: String)   extends AnyVal
case class Title(raw: String)    extends AnyVal
case class CreditCard(number: String, expirationDate: String)
case class Person(name: String, age: Int)
case class Government(name: String, debt: BigDecimal)
case class Book(category: Category, author: Author, title: Title, price: Price)

case class Price(amount: BigDecimal, currency: Currency)
