package com.github.hd.tornadofxsuite.view

import com.github.hd.tornadofxsuite.app.Styles
import com.github.hd.tornadofxsuite.controller.FXTestGenerator
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.util.Duration
import tornadofx.*
import java.io.File

class FetchCompletedEvent : FXEvent()

class MainView : View() {
    val consolePath = System.getProperty("os.name") + " ~ " + System.getProperty("user.name") + ": "
    lateinit var console: ListView<String>
    private val fxTestGenerator: FXTestGenerator by inject()
    lateinit var overlay: HBox

    override val root = stackpane {
        vbox {
            prefWidth = 800.0
            prefHeight = 600.0

            hbox {
                imageview("file:tornado-fx-logo.png")
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
                        fxTestGenerator.walk(it.absolutePath)
                        fxTestGenerator.askUserDialog()
                        //fxTestGenerator.fetchAsync(it)
                    }
                    fxTestGenerator.kotlinFiles.forEach { println(it) }
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
            style {
                backgroundColor += c("#222")
                opacity = 0.0
            }
        }
    }

}