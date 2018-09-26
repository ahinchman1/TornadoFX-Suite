package com.github.hd.tornadofxsuite.view

import com.github.hd.tornadofxsuite.app.Styles
import com.github.hd.tornadofxsuite.app.Styles.Companion.translucent
import com.github.hd.tornadofxsuite.controller.FXTestGenerator
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import tornadofx.*
import java.io.File

class FetchCompletedEvent : FXEvent()

class MainView : View("TornadoFX-Suite") {
    val consolePath = System.getProperty("os.name") + " ~ " + System.getProperty("user.name") + ": "
    lateinit var console: ListView<String>
    private val fxTestGenerator: FXTestGenerator by inject()
    lateinit var overlay: HBox

    override val root = stackpane {
        overlay = hbox {
            prefWidth = 800.0
            prefHeight = 600.0
        }
        vbox {
            prefWidth = 800.0
            prefHeight = 600.0

            hbox {
                imageview("tornado-fx-logo.png")
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
                    subscribe<FetchCompletedEvent> {
                        this@listview.translateYProperty().animate(endValue = 0.0, duration = .3.seconds)
                    }
                }
            }

            button("Upload your project directory.") {
                setOnAction {
                    chooseDirectory {
                        title = "Choose a TornadoFX Project"
                        initialDirectory = File(System.getProperty("user.home"))
                    }?.let {
                        fxTestGenerator.walk(it.absolutePath)
                        //fxTestGenerator.fetchAsync(it)
                    }
                    fxTestGenerator.kotlinFiles.forEach { println(it) }
                }
                vboxConstraints {
                    marginLeft = 300.0
                    marginBottom = 40.0
                }
            }

        }.addClass(Styles.mainScreen)
    }

}