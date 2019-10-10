package com.github.ast.parser

import com.google.gson.JsonObject

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
        "RANGE" -> ".."
        "AS" -> " as "
        "ADD" -> " + "
        "SUB" -> " - "
        else -> token
    }
}

/***
 * Kastree readOnly values indicates whether a value is mutable or immutable.
 *
 * @param node: [JsonObject] - node property
 * @param isolated: [JsonObject] - for potential dependency injection
 * @param isolatedName: [String] - name assigned to property
 *
 * @return [String] 'val' or 'var'
 */
fun valOrVar(node: JsonObject): String = if (node.readOnly()) "val" else "var"

fun getGenericTypeArgs(node: JsonObject): String {
    var result = ""
    if (node.typeArgs().size() > 0) {
        result = "<"
        val genericType = node.typeArgs()

        for (i in 0 until genericType.size()) {
            result += genericType.getObject(i).ref().getType()
            if (i < genericType.size()) {
                result += ", "
            }
        }

        result += ">"
    }

    return result
}