package com.github.ast.parser

import java.util.*

data class ClassBreakDown(val className: String,
                          val classProperties: ArrayList<ClassProperties>,
                          val classMethods: ArrayList<String>)

data class ClassProperties(val propertyName: String,
                           val propertyType: String)





