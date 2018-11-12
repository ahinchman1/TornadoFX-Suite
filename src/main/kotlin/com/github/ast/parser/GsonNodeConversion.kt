package com.github.ast.parser

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class GsonASTConversion(val node: JsonObject) {

    fun JsonObject.expr(): JsonObject {
        return this.get("expr").asJsonObject
    }

    fun JsonObject.ref(): JsonObject {
        return this.get("ref").asJsonObject
    }

    fun JsonObject.typeArgs(): JsonArray {
        return this.get("typeArgs").asJsonArray
    }

    fun JsonObject.pieces(): JsonArray {
        return this.get("pieces").asJsonArray
    }

    fun JsonObject.getName(): String {
        return this.get("name").asString
    }

}