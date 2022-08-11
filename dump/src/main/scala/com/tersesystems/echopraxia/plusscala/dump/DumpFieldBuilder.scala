package com.tersesystems.echopraxia.plusscala.dump

import com.tersesystems.echopraxia.api.Value

import scala.language.experimental.macros

trait DumpFieldBuilder {
  import DumpFieldBuilder.impl

  def dumpPublicFields[A](instance: A): Value.ObjectValue = macro impl.dumpPublicFields[A]
}

object DumpFieldBuilder extends DumpFieldBuilder {

    import scala.reflect.macros.blackbox

    private class impl(val c: blackbox.Context) {

      import c.universe._

      def dumpPublicFields[A: WeakTypeTag](instance: c.Expr[A]): c.Expr[Value.ObjectValue] = {
        def classVals(tpe: c.universe.Type) = {
          tpe.decls.collect {
            case method: MethodSymbol if method.isAccessor && method.isPublic =>
              val nameStr = method.name.decodedName.toString
              q"""com.tersesystems.echopraxia.api.Field.keyValue($nameStr,
                     com.tersesystems.echopraxia.api.Value.string(java.util.Objects.toString($instance.$method)))
                     .asInstanceOf[com.tersesystems.echopraxia.api.Field]"""
          }
        }

        val classType = weakTypeTag[A].tpe
        val fields = classVals(classType)

        c.Expr[Value.ObjectValue](q"com.tersesystems.echopraxia.api.Value.`object`(java.util.Arrays.asList(..$fields))")
      }
    }
}
