package com.yg.vs

trait JdbcTypesComponent { self: JdbcProfile =>
  trait ImplicitColumnTypes {
    implicit def intColumnType = columnTypes.intJdbcType
    implicit def stringColumnType = columnTypes.stringJdbcType
  }
}

trait JdbcProfile {
  val columnTypes = new JdbcTypes
}

class JdbcTypes {
  val intJdbcType = new IntJdbcType
  val stringJdbcType = new StringJdbcType

  class IntJdbcType extends BaseTypedType[Int]
  class StringJdbcType extends BaseTypedType[String]

}

