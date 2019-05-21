package com.github.ast.parser

import com.google.gson.JsonArray
import com.google.gson.JsonObject

fun JsonObject.getType(): String = this.pieces().getObject(0).name()

fun JsonObject.getGenericTypeArgument(num: Int): String = this.expr().typeArgs().getObject(num).ref().getType()

fun JsonObject.lambda(): JsonObject = this.get("lambda").asJsonObject

fun JsonObject.expr(): JsonObject = this.get("expr").asJsonObject

fun JsonObject.ref(): JsonObject = this.get("ref").asJsonObject

fun JsonObject.type(): JsonObject = this.get("type").asJsonObject

fun JsonObject.func(): JsonObject = this.get("func").asJsonObject

fun JsonObject.block(): JsonObject = this.get("block").asJsonObject

fun JsonObject.stmts(): JsonArray = this.get("stmts").asJsonArray

fun JsonArray.getObject(num: Int): JsonObject = this.get(num).asJsonObject

fun JsonObject.vars(): JsonArray = this.get("vars").asJsonArray

fun JsonObject.args(): JsonArray = this.get("args").asJsonArray

fun JsonObject.typeArgs(): JsonArray = this.get("typeArgs").asJsonArray

fun JsonObject.pieces(): JsonArray = this.get("pieces").asJsonArray

fun JsonObject.lhs(): JsonObject = this.get("lhs").asJsonObject

fun JsonObject.rhs(): JsonObject = this.get("rhs").asJsonObject

fun JsonObject.elems(): JsonArray = this.get("elems").asJsonArray

fun JsonObject.body(): JsonObject = this.get("body").asJsonObject

fun JsonObject.decl(): JsonObject = this.get("decl").asJsonObject

fun JsonObject.decls(): JsonArray = this.get("decls").asJsonArray

fun JsonObject.oper(): JsonObject = this.get("oper").asJsonObject

fun JsonObject.params(): JsonArray = this.get("params").asJsonArray

fun JsonObject.recv(): JsonObject = this.get("recv").asJsonObject

// primitives

fun JsonObject.token(): String = this.get("token").asString

fun JsonObject.readOnly(): Boolean = this.get("readOnly").asBoolean

fun JsonObject.delegated(): Boolean = this.get("delegated").asBoolean

fun JsonObject.str(): String = this.get("str").asString

fun JsonObject.name(): String = this.get("name").asString

fun JsonObject.form(): String = this.get("form").asString

// next check

fun JsonObject.hasBody(): Boolean = this.has("body")

fun JsonObject.hasBlock(): Boolean = this.has("block")

fun JsonObject.hasParameters(): Boolean = this.has("params")

fun JsonObject.hasDeclaration(): Boolean = this.has("decl")

fun JsonObject.hasName(): Boolean = this.has("name")

fun JsonObject.hasString(): Boolean = this.has("str")

fun JsonObject.hasToken(): Boolean = this.has("token")

fun JsonObject.hasValue(): Boolean = this.has("value")

fun JsonObject.hasExpression(): Boolean = this.has("expr")

fun JsonObject.hasPrimitiveValue(): Boolean = this.has("value")

fun JsonObject.hasBinaryOperation(): Boolean = this.has("lhs") && this.has("oper") && this.has("rhs")

fun JsonObject.hasReceiver(): Boolean = this.has("recv")

fun JsonObject.hasElements(): Boolean = this.has("elems")

fun JsonObject.hasLambda(): Boolean = this.has("lambda")

fun JsonObject.hasFunc(): Boolean = this.has("func")

fun JsonObject.hasArguments(): Boolean = this.has("args")

fun JsonObject.hasTypeArguments(): Boolean = this.has("typeArgs")

fun JsonObject.hasExpressionCall(): Boolean = this.hasExpression() && this.hasTypeArguments() && this.hasArguments()