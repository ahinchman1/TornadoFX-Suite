package com.github.hd.tornadofxsuite.controller

import com.github.hd.tornadofxsuite.app.PopulateAstParsingToConsole
import com.github.hd.tornadofxsuite.app.ReadKotlinScripting
import kastree.ast.psi.Parser
import tornadofx.*

class AstParserDiagramController : Controller() {
  init {
    subscribe<ReadKotlinScripting> { event ->
      val file = Parser.parseFile(event.textFile, true)

      generateAstTree()
    }
  }

  private fun generateAstTree() {
    populateConsole("Success!", listOf("Now you just have to parse the tree and render the tree"))
  }

  private fun populateConsole(fileName: String, astTree: List<String>) {
    fire(PopulateAstParsingToConsole(fileName, astTree))
  }
}
