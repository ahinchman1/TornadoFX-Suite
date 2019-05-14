package com.github.ast.parser

import com.google.gson.JsonObject

class KParserUtils {

    fun getPrimitiveValue(value: JsonObject): String {
        val gValue = value.get("value")
        return when (value.get("form").asJsonPrimitive.toString()) {
            "\"BOOLEAN\"" ->  gValue.asBoolean.toString()
            "\"BYTE\"" -> gValue.asByte.toString()
            "\"CHAR\"" -> gValue.asCharacter.toString()
            "\"DOUBLE\"" -> gValue.asDouble.toString()
            "\"FLOAT\"" -> gValue.asFloat.toString()
            "\"INT\"" -> gValue.asInt.toString()
            "\"NULL\"" -> "null"
            else -> "Unrecognized value type"
        }
    }

    // TODO rewrite to accept 2 types for primitive
    fun getPrimitiveType(form: JsonObject): String {
        return when (form.get("form").asJsonPrimitive.toString()) {
            "\"BOOLEAN\"" -> "Boolean"
            "\"BYTE\"" -> "Byte"
            "\"CHAR\"" -> "Char"
            "\"DOUBLE\"" -> "Double"
            "\"FLOAT\"" -> "Float"
            "\"INT\"" -> "Int"
            "\"NULL\"" -> "null"
            else -> "Unrecognized value type" // object type probs
        }
    }

    fun getPrimitiveType(form: String): String {
        return when (form) {
            "\"BOOLEAN\"" -> "Boolean"
            "\"BYTE\"" -> "Byte"
            "\"CHAR\"" -> "Char"
            "\"DOUBLE\"" -> "Double"
            "\"FLOAT\"" -> "Float"
            "\"INT\"" -> "Int"
            "\"NULL\"" -> "null"
            else -> "Unrecognized value type" // object type probs
        }
    }

    fun getToken(token: String): String {
        return when (token) {
            "DOT" -> "."
            "ASSN" -> " = "
            "NEQ" -> " != "
            "NEG" -> "-"
            "EQ" -> " == "
            "RANGE" -> " "
            "AS" -> " as "
            else -> token
        }
    }
}