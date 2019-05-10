package com.github.ast.parser.frameworkconfigurations

import com.github.ast.parser.KParserImpl
import com.google.gson.JsonObject

// TODO - make these type alias more generalized
typealias ComponentBreakdownFunction = KParserImpl.(String, String) -> Unit

typealias DetectFrameworkComponents = KParserImpl.(String, String, JsonObject) -> Unit

// TODO - change KParserImpl to be able to use any subclass of Views
sealed class Views(var viewClass: String? = "", var viewType: String? = "")

data class TornadoFXView(
        var view: String? = "",
        var type: String? = "",
        var scope: String? = ""
) : Views(view, type)