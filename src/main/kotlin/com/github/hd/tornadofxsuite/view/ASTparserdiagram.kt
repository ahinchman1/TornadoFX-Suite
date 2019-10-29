package com.github.hd.tornadofxsuite.view

import com.github.ast.parser.KParserImpl
import com.github.ast.parser.frameworkconfigurations.detectRoot
import com.github.ast.parser.frameworkconfigurations.detectScopes
import com.github.ast.parser.frameworkconfigurations.saveComponentBreakdown
import com.github.hd.tornadofxsuite.app.ReadKotlinScripting
import com.github.hd.tornadofxsuite.app.Styles
import javafx.geometry.Pos
import javafx.scene.control.TextArea
import javafx.scene.layout.HBox
import kastree.ast.psi.Parser
import tornadofx.*
import java.util.HashMap

class AstParserDiagram : View("AST Parsing Visual Repesentation") {

  private lateinit var overlay: HBox
  private lateinit var console: TextArea

  var scanner = KParserImpl(
    "",
    KParserImpl::saveComponentBreakdown,
    HashMap(),
    KParserImpl::detectScopes,
    KParserImpl::detectRoot
  )

  override val root = stackpane {
    vbox {
      prefWidth = 600.0
      prefHeight = 400.0

      menubar {
        menu("File") {
          item("Return to UI test generation").action {
            replaceWith<MainView>()
          }
        }
      }

      hbox {
        hboxConstraints {
          marginLeftRight(20.0)
          marginTopBottom(20.0)
        }
      }.addClass(Styles.top)

      stackpane {
        vboxConstraints {
          marginTopBottom(40.0)
          marginLeftRight(40.0)
        }
        vbox {
          label("Fetching")
          progressindicator()
          alignment = Pos.TOP_CENTER
          spacing = 4.0
          paddingTop = 10.0
        }
        console = textarea {
          subscribe<ReadKotlinScripting> { event ->
            val file = Parser.parseFile(event.textFile, true)
          }
        }
      }

      button("Show AST breakdown") {
        id = "compileAndRenderAST"

        setOnAction {
          fire(ReadKotlinScripting(console.text))
        }

        vboxConstraints {
          marginLeft = 150.0
          marginBottom = 20.0
        }
      }

    }.addClass(Styles.main)

    overlay = hbox {
      prefWidth = 600.0
      prefHeight = 400.0
      isMouseTransparent = true
    }.addClass(Styles.transparentLayer)
  }
}
