package com.github.hd.tornadofxsuite.controller

import com.example.demo.controller.ClassScanner
import com.github.hd.tornadofxsuite.app.Styles.Companion.translucent
import com.github.hd.tornadofxsuite.model.TornadoFXInputsScope
import com.github.hd.tornadofxsuite.view.Dialog
import com.github.hd.tornadofxsuite.view.FetchCompletedEvent
import com.github.hd.tornadofxsuite.view.MainView
import com.github.mattmoore.kast.parse
import javafx.scene.layout.StackPane
import javafx.util.Duration
import tornadofx.*
import java.io.BufferedReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FXTestGenerator: Controller() {
    val kotlinFiles = ArrayList<File>()
    private val view: MainView by inject()
    private val scanner: ClassScanner by inject()

    // TODO separate UI from AST parsing
    fun fetchAsync(file: File) {
        runAsync {
            walk(file.absolutePath)
        } ui {
            fire(FetchCompletedEvent())
        } fail {
            println( "Cannot read file: $file")
        }
    }

     fun walk(path: String) {
        Files.walk(Paths.get(path)).use { allFiles ->
            allFiles.filter { path -> path.toString().endsWith(".kt") }
                    .forEach {
                        fileOutputRead(it)
                    }
        }
        consoleLog()
    }

    private fun fileOutputRead(path: Path) {
        val file = File(path.toUri())
        readFiles(file)
    }

    private fun consoleLog() {
        // print and format classes
        scanner.bareClasses.forEach {
            println("CLASS NAME: " + it.className)
            println("CLASS PROPERTIES: ")
            it.classProperties.forEach { property ->
                println("\t" + property.propertyName + ": " + property.propertyType)
            }
            println("CLASS METHODS: ")
            it.classMethods.forEach { method ->
                println("\t" + method)
            }
        }

        if (scanner.detectedViewControls.size > 0) {
            println("DETECTED LAMBDA ELEMENTS IN PROJECT: ")
            scanner.detectedViewControls.forEach {
                println(it)
            }
        }

        if (scanner.detectedViewControls.size > 0) {
            println("DERIVING INPUTS: ")
            scanner.detectedViewControls.forEach {
                println(it)
            }
        }
    }

    private fun readFiles(file: File) {
        val fileText = file.bufferedReader().use(BufferedReader::readText)
        if (filterFiles(fileText)) {
            view.console.items.add(view.consolePath + file.toString())
            view.console.items.add("READING FILES...")
            kotlinFiles.add(file)
            view.console.items.add(fileText)
            view.console.items.add("===================================================================")
            //scanner.parseAST(fileText)
            com.github.mattmoore.kast.parse(fileText)
        }
    }

    // TODO: Either use regex or better parsing
    // filter files for only Views and Controllers
    private fun filterFiles(fileText: String): Boolean {
        var arr = arrayOf(1,2,3)
        var array = arrayOf("string", "whatevers")
        var com = arrayOf("sa", 1)

        return !fileText.contains("ApplicationTest()")
                && !fileText.contains("src/test")
                && !fileText.contains("@Test")
    }

    fun askUserDialog() {
        view.overlay.fade(Duration.millis(2000.0), .5)
        find(Dialog::class).openModal()
    }
}