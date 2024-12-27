package echopraxia.plusscala.api

trait FindPathMethods {
  def findString(jsonPath: String): Option[String]

  def findBoolean(jsonPath: String): Option[Boolean]

  def findNumber(jsonPath: String): Option[Number]

  def findNull(jsonPath: String): Boolean

  def findThrowable(jsonPath: String): Option[Throwable]

  def findThrowable: Option[Throwable]

  def findObject(jsonPath: String): Option[Map[String, Any]]

  def findList(jsonPath: String): Seq[Any]
}
