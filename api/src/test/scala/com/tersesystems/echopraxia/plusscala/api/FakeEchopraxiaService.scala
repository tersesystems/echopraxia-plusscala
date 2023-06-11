package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{AbstractEchopraxiaService, CoreLogger, EchopraxiaService, EchopraxiaServiceProvider}

class FakeEchopraxiaServiceProvider extends EchopraxiaServiceProvider {
  override lazy val getEchopraxiaService: EchopraxiaService = new FakeEchopraxiaService
}

class FakeEchopraxiaService extends AbstractEchopraxiaService {
  override def getCoreLogger(fqcn: String, clazz: Class[_]): CoreLogger = ???

  override def getCoreLogger(fqcn: String, name: String): CoreLogger = ???
}
