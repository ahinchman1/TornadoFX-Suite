package com.github.ast.parser

import com.google.gson.JsonArray
import com.google.gson.JsonObject

fun JsonObject.getType() = this.pieces().getObject(0).name()

fun JsonObject.getCollectionType(num: Int) = this.expr().typeArgs().getObject(num).ref().getType()

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