
## Custom Logger

You can create a custom logger which has your own methods and field builders by extending `DefaultLoggerMethods`.

```scala
import echopraxia.api._
import echopraxia.plusscala.DefaultLoggerMethods
import echopraxia.plusscala.api._

object CustomLoggerFactory {
  private val FQCN: String = classOf[DefaultLoggerMethods[_]].getName
  private val fieldBuilder: FooBuilder.type = FooBuilder

  def getLogger(name: String): CustomLogger[FooBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new CustomLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): CustomLogger[FooBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new CustomLogger(core, fieldBuilder)
  }

  def getLogger: CustomLogger[FooBuilder.type] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new CustomLogger(core, fieldBuilder)
  }
}

class CustomLogger[FB](val core: CoreLogger, val fieldBuilder: FB) extends Logger[FB] with DefaultLoggerMethods[FB] {

  override def name: String = core.getName
  
  // add a custom method here for logger.debug(msg, "foo" -> foo, "bar" -> bar)   
  def debug[A1: fieldBuilder.ToValue, A2: fieldBuilder.ToValue](message: String, a1: (String, A1), a2: (String, A2)): Unit = {
    debug(message, fb => fb.list(
      fieldBuilder.keyValue(a1._1, a1._2),
      fieldBuilder.keyValue(a2._1, a2._2)
    ))
  }


  override def withCondition(condition: Condition): CustomLogger[FB] = {
    condition match {
      case Condition.always =>
        this
      case Condition.never =>
        newLogger(newCoreLogger = core.withCondition(Condition.never.asJava), fieldBuilder)
      case other =>
        newLogger(newCoreLogger = core.withCondition(other.asJava))
    }
  }

  override def withFields(f: FB => FieldBuilderResult): CustomLogger[FB] = {
    newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
  }

  override def withThreadContext: CustomLogger[FB] = {
    newLogger(
      newCoreLogger = core.withThreadContext(Utilities.threadContext())
    )
  }

  override def withFieldBuilder[NEWFB](newFieldBuilder: NEWFB): CustomLogger[NEWFB] = {
    newLogger(newFieldBuilder = newFieldBuilder)
  }

  @inline
  private def newLogger[T](
                            newCoreLogger: CoreLogger = core,
                            newFieldBuilder: T = fieldBuilder
                          ): CustomLogger[T] =
    new CustomLogger[T](newCoreLogger, newFieldBuilder)

}
```

Creating a custom logger can be a good way to ensure that your field builder is used without any extra configuration, and lets you add your own methods and requirements for your application.

You can also provide your own logger from scratch if you want, by only using the API dependency -- this ensures that users only have one logging option, and you can add your own metrics and observability around your logging.

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "api" % echopraxiaPlusScalaVersion
```
