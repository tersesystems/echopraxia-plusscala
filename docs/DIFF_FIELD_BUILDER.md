
## Diff Field Builder

The diff field builder takes two values that have `ToValue` type classes, and returns the difference between them in [RFC 6902](https://datatracker.ietf.org/doc/html/rfc6902) format.  It is especially useful for debugging changes in deeply nested objects and trees, and can cut down on the amount of "noise" of debug logging.

To import the diff field builder add the following to `build.sbt`:

```
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "diff" % echopraxiaPlusScalaVersion
```

and then call the diff method:

```scala
import com.tersesystems.echopraxia.plusscala.diff.DiffFieldBuilder

trait MyFieldBuilder extends DiffFieldBuilder with FieldBuilder {
  implicit val personToObjectValue: ToObjectValue[Person] = (p: Person) =>
  ToObjectValue(
    string("name", p.name),
    number("age", p.age)
  )
}
object MyFieldBuilder extends MyFieldBuilder
  
val logger = LoggerFactory.getLogger.withFieldBuilder(MyFieldBuilder)

val person1 = Person("person1", 13)
val person2 = Person("person2", 13)
logger.info("{}", _.diff("personDiff", person1, person2))
```

The diff field builder uses Jackson 2.13.3 internally.
