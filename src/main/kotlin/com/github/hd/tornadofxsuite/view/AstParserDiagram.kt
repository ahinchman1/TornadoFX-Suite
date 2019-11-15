package com.github.hd.tornadofxsuite.view

import com.github.hd.tornadofxsuite.app.PopulateAstParsingToConsole
import com.github.hd.tornadofxsuite.app.ReadKotlinScripting
import com.github.hd.tornadofxsuite.app.Styles
import javafx.scene.control.TextArea
import javafx.scene.layout.HBox
import tornadofx.*

class AstParserDiagram : View("AST Parsing Visual Repesentation") {
  private lateinit var overlay: HBox
  private lateinit var console: TextArea

  override val root = stackpane {
    vbox {
      prefWidth =  800.0
      prefHeight = 600.0

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
        hbox {
          console = textarea("Fill out Kotlin code here:") {
            hboxConstraints {
              marginRight = 20.0
              prefWidth = 330.0
            }
          }
          listview<String> {
            subscribe<PopulateAstParsingToConsole> { event ->
              println(event)
              items.add("SUCCESS....")
              items.add(event.file)
              items.addAll(event.tree)
            }
            items.add("AST Parsing renders here:")
            hboxConstraints {
              marginLeft = 20.0
              prefWidth = 330.0
            }
          }
        }
      }.addClass(Styles.main)

      button("Show AST breakdown") {
        id = "compileAndRenderAST"

        setOnAction {
          fire(ReadKotlinScripting(console.text))
        }

        vboxConstraints {
          marginLeft = 300.0
          marginBottom = 40.0
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
