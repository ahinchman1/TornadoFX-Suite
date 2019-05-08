package com.github.ast.parser

sealed class Views(var viewClass: String? = "", var viewType: String? = "")

data class TornadoFXView(
        var view: String? = "",
        var type: String? = "",
        var scope: String? = ""
) : Views(view, type)