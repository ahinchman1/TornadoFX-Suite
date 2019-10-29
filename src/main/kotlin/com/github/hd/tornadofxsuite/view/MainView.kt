package com.github.hd.tornadofxsuite.view

import com.github.ast.parser.nodebreakdown.MapKClassTo
import com.github.ast.parser.nodebreakdown.TestClassInfo
import com.github.hd.tornadofxsuite.app.*
import com.github.hd.tornadofxsuite.controller.FXTestGenerator
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.stage.DirectoryChooser
import javafx.util.Duration
import tornadofx.*
import tornadofx.Stylesheet.Companion.menu
import java.io.File

class MainView : View() {
    private val testGenerator: FXTestGenerator by inject()
    private val consolePath = System.getProperty("os.name") + " ~ " + System.getProperty("user.name") + ": "

    val classesTestInfo = MapKClassTo<TestClassInfo>()

    private lateinit var console: ListView<String>
    lateinit var overlay: HBox
    lateinit var directoryChooser: DirectoryChooser

    init {
        subscribe<ReadFilesRequest> { event ->
            testGenerator.walk(event.file.absolutePath)
        }

        subscribe<OnParsingComplete> { event ->
            // TODO work on UI for loading smh make it pretty
            fire(MapNodesToFunctionsRequest(event.viewTestClassInfo, event.classBreakDown))
            // TODO move this out into another fxevent for when mapping is complete
            classesTestInfo.putAll(event.viewTestClassInfo)
            askUserDialog()
        }
    }

    override val root = stackpane {
        vbox {
            prefWidth = 800.0
            prefHeight = 600.0

            menubar {
                menu("File") {
                    item("AST syntax compiler").action {
                        replaceWith<AstParserDiagram>()
                    }
                }
            }

            hbox {
                // imageview("tornado-fx-logo.png")
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
                console = listview {
                    id = "console"

                    items.add(consolePath)
                    subscribe<PrintFileToConsole> { event ->
                        writeFileToConsole(event.file, event.textFile)
                    }
                }
            }

            button("Upload your project.") {
                id = "uploadProject"

                setOnAction {
                    overlay.fade(Duration.millis(2000.0), .5)
                    chooseDirectory {
                        id = "directoryChooser"
                        title = "Choose a TornadoFX Project"
                        initialDirectory = File(System.getProperty("user.home"))
                        directoryChooser = this
                    }?.let {
                        console.items.clear()
                        console.items.add("SEARCHING FILES...")
                        fire(ReadFilesRequest(it))
                    }
                }
                vboxConstraints {
                    marginLeft = 300.0
                    marginBottom = 40.0
                }
            }

        }.addClass(Styles.main)

        overlay = hbox {
            prefWidth = 800.0
            prefHeight = 600.0
            isMouseTransparent = true
        }.addClass(Styles.transparentLayer)
    }

    private fun writeFileToConsole(file: String, fileText: String) {
        console.items.apply{
            add(consolePath + file)
            add("READING FILES...")
            add(fileText)
            add("===================================================================")
        }
    }

    private fun askUserDialog() {
        overlay.fade(Duration.millis(2000.0), .5)
        find(Dialog::class).openModal()
    }

}