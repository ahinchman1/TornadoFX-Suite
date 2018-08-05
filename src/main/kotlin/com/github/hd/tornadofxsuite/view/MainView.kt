package com.github.hd.tornadofxsuite.view

import com.github.hd.tornadofxsuite.app.Styles
import javafx.scene.control.ListView
import tornadofx.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths



class MainView : View("TornadoFX-Suite") {
    private val kotlinFiles = ArrayList<File>()
    private val consolePath = System.getProperty("os.name") + " ~ " + System.getProperty("user.name") + ": "
    private lateinit var console: ListView<String>

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
                    walk(it.absolutePath)
                }
                kotlinFiles.forEach { println(it) }
            }
            vboxConstraints {
                marginLeft = 300.0
                marginBottom = 40.0
            }
        }

    }.addClass(Styles.mainScreen)

    private fun walk(path: String) {
        console.items.clear()
        console.items.add("SEARCHING FILES...")
        Files.walk(Paths.get(path)).use { allFiles ->
            allFiles.filter { path -> path.toString().endsWith(".kt") }
                    .forEach {
                        fileOutputRead(it)
                    }
        }
    }

    private fun fileOutputRead(path: Path) {
        val file = File(path.toUri())
        println(path)
        filterFiles(file)
    }

    // filter files for only Views and Controllers
    private fun filterFiles(file: File) {
        val fileText = file.bufferedReader().use(BufferedReader::readText)
        if (!fileText.contains("ApplicationTest()")) {
            console.items.add(consolePath + file.toString())
            console.items.add("READING FILES..")
            kotlinFiles.add(file)
            console.items.add(fileText)
        }
    }

}