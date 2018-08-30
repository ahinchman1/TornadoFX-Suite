package com.github.hd.tornadofxsuite.model

import java.util.*

data class ClassBreakDown(val className: String,
                          val classAccessLevel: String? = "public",
                          val classFields: ArrayList<Property>,
                          val classDependencies: ArrayList<DependencyInjection>,
                          val classMethods: ArrayList<Function>)

data class Property(val name: String,
                    val accessLevel: String,
                    val type: String,
                    val value: Any)

data class DependencyInjection(val name: String,
                    val accessLevel: String,
                    val dependency: String)

data class Function(val methodName: String,
                     val methodAccess: String = "public",
                     val methodParams: ArrayList<Parameters>,
                     val returnsType: String = "void")

data class Parameters(val paramName: String,
                      val paramType: String)