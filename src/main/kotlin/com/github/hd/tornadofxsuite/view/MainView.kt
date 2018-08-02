package com.github.hd.tornadofxsuite.view

import com.github.hd.tornadofxsuite.app.Styles
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths



class MainView : View("TornadoFX-Suite") {
    private val kotlinFiles = ArrayList<File>()
    private val consolePath = System.getProperty("os.name") + " ~ " + System.getProperty("user.name") + ": "
    private val console = textflow().addClass(Styles.console)

    override val root = vbox {
        prefWidth = 400.0
        prefHeight = 600.0

        hbox {
            imageview("tornado-fx-logo.png")
            hboxConstraints {
                marginLeftRight(20.0)
                marginTopBottom(20.0)
            }
        }.addClass(Styles.top)

        scrollpane {
            prefWidth = 200.0
            prefHeight = 200.0
            console
            vboxConstraints {
                marginTopBottom(60.0)
                marginLeftRight(40.0)
            }
        }

        button("Upload your project directory.") {
            setOnAction {
                chooseDirectory {
                    title = "Choose a TornadoFX Project"
                    initialDirectory = File(System.getProperty("user.home"))
                }?.let {
                    walk(it.absolutePath)
                }
                kotlinFiles.forEach { println(it) }
            }
            vboxConstraints {
                marginLeft = 100.0
                marginTop = 20.0
            }
        }

    }.addClass(Styles.mainScreen)

    private fun walk(path: String) {
        Files.walk(Paths.get(path)).use { allFiles ->
            allFiles.filter { path -> path.toString().endsWith(".kt") }
                    .forEach {
                        fileOutputRead(it)
                    }
        }
    }

    private fun fileOutputRead(file: Path) {
        kotlinFiles.add(File(file.toUri()))
        println(file)
        console.add(text(consolePath + file.toString()) {
            fill = Color.WHITE
        })
    }
}