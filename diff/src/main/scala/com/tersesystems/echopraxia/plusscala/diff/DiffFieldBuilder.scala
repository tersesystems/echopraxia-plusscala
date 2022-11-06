package com.tersesystems.echopraxia.plusscala.diff

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.flipkart.zjsonpatch.JsonDiff
import com.tersesystems.echopraxia.api.{Field, Value}
import com.tersesystems.echopraxia.plusscala.api.ValueTypeClasses

/**
 * This field builder uses the stable structured representation of objects to diff them against each other.
 *
 * It depends on Jackson and zjsonpatch internally.
 */
trait DiffFieldBuilder extends ValueTypeClasses {
  import DiffFieldBuilder._

  /**
   * Diff two values against each other.
   *
   * @param fieldName
   *   the field name to give the diff
   * @param before
   *   object before (aka `base`)
   * @param after
   *   object after (aka `working`)
   * @tparam T
   *   the type of the object
   * @return
   *   the field representing the diff between the two values.
   */
  def diff[T: ToValue](fieldName: String, before: T, after: T): Field = {
    // XXX should add this to Java version and then map it through
    val beforeNode: JsonNode = mapper.valueToTree(ToValue(before))
    val afterNode: JsonNode  = mapper.valueToTree(ToValue(after))
    val patch: JsonNode      = JsonDiff.asJson(beforeNode, afterNode)
    // convert the patch json node back to fields and values :-)
    val value: Value[_] = mapper.convertValue(patch, classOf[Value[_]])
    Field.keyValue(fieldName, value)
  }
}

object DiffFieldBuilder extends DiffFieldBuilder {
  private val mapper = new ObjectMapper()
  mapper.findAndRegisterModules
}
