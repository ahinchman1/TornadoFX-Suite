package com.github.hd.tornadofxsuite.view

import com.github.hd.tornadofxsuite.app.Styles
import com.github.hd.tornadofxsuite.controller.FXTestGenerator
import javafx.scene.control.ListView
import tornadofx.*
import java.io.File

class MainView : View("TornadoFX-Suite") {
    val consolePath = System.getProperty("os.name") + " ~ " + System.getProperty("user.name") + ": "
    lateinit var console: ListView<String>
    private val fxTestGenerator: FXTestGenerator by inject()

    override val root = vbox {
        prefWidth = 800.0
        prefHeight = 600.0

        hbox {
            imageview("tornado-fx-logo.png")
            hboxConstraints {
                marginLeftRight(20.0)
                marginTopBottom(20.0)
            }
        }.addClass(Styles.top)

        console = listview {
            items.add(consolePath)
            vboxConstraints {
                marginTopBottom(40.0)
                marginLeftRight(40.0)
            }
        }

        button("Upload your project directory.") {
            setOnAction {
                chooseDirectory {
                    title = "Choose a TornadoFX Project"
                    initialDirectory = File(System.getProperty("user.home"))
                }?.let {
                    fxTestGenerator.walk(it.absolutePath)
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