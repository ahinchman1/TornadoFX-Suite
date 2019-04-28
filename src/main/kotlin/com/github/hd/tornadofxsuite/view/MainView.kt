package com.github.hd.tornadofxsuite.view

import com.github.ast.parser.TestClassInfo
import com.github.hd.tornadofxsuite.app.Styles
import com.github.hd.tornadofxsuite.controller.FXTestGenerator
import com.github.hd.tornadofxsuite.controller.OnParsingComplete
import com.github.hd.tornadofxsuite.controller.PrintFileToConsole
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.util.Duration
import tornadofx.*
import java.io.File

class ReadFilesRequest(val file: File) : FXEvent(EventBus.RunOn.BackgroundThread)

class MainView : View() {
    private val testGenerator: FXTestGenerator by inject()
    val classesTestInfo = ArrayList<TestClassInfo>()
    val consolePath = System.getProperty("os.name") + " ~ " + System.getProperty("user.name") + ": "
    lateinit var console: ListView<String>
    lateinit var overlay: HBox

    init {
        subscribe<ReadFilesRequest> { event ->
            testGenerator.walk(event.file.absolutePath)
        }

        subscribe<OnParsingComplete> { event ->
            classesTestInfo.addAll(event.testClassInfo)
            askUserDialog()
        }
    }

    override val root = stackpane {
        vbox {
            prefWidth = 800.0
            prefHeight = 600.0

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
                    items.add(consolePath)
                    subscribe<PrintFileToConsole> { event ->
                        writeFileToConsole(event.file, event.textFile)
                    }
                }
            }

            button("Upload your project.") {
                setOnAction {
                    overlay.fade(Duration.millis(2000.0), .5)
                    chooseDirectory {
                        title = "Choose a TornadoFX Project"
                        initialDirectory = File(System.getProperty("user.home"))
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
        console.items.add(consolePath + file)
        console.items.add("READING FILES...")
        console.items.add(fileText)
        console.items.add("===================================================================")
    }

    private fun askUserDialog() {
        overlay.fade(Duration.millis(2000.0), .5)
        find(Dialog::class).openModal()
    }

}