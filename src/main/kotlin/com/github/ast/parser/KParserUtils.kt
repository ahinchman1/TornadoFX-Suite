package com.github.ast.parser

import com.google.gson.JsonObject

fun KParserImpl.getPrimitiveValue(value: JsonObject): String {
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
fun KParserImpl.getPrimitiveType(form: JsonObject): String {
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

fun KParserImpl.getPrimitiveType(form: String): String {
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

fun KParserImpl.getToken(token: String): String {
    return when (token) {
        "DOT" -> "."
        "ASSN" -> " = "
        "NEQ" -> " != "
        "NEG" -> " - "
        "EQ" -> " == "
        "RANGE" -> ".."
        "AS" -> " as "
        "ADD" -> " + "
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
fun KParserImpl.valOrVar(node: JsonObject): String = if (node.readOnly()) "val " else "var "

fun KParserImpl.getGenericTypeArgs(node: JsonObject): String {
    var result = "<"
    val genericType = node.expr().typeArgs()

    for (i in 0..genericType.size()) {
        result += genericType.getObject(i).ref().getType()
        if (i < genericType.size()) {
            result += ", "
        }
    }

    return "$result>"
}