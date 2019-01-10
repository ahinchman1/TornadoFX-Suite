package com.github.hd.tornadofxsuite.controller

import com.github.ast.parser.KParser
import com.github.hd.tornadofxsuite.view.Dialog
import com.github.hd.tornadofxsuite.view.FetchCompletedEvent
import com.github.hd.tornadofxsuite.view.MainView
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
    private val scanner: KParser by inject()

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
        /*scanner.classes.forEach {
            println("CLASS NAME: " + it.className)
            println("CLASS PROPERTIES: ")
            it.classProperties.forEach { property ->
                println("\t" + property.propertyName + ": " + property.propertyType)
            }
            println("CLASS METHODS: ")
            var buildMethods = ""
            it.classMethods.forEach { method ->
                buildMethods += "-----------------\n|\tname:${method.name}\n|\tparameters: "
                    method.parameters.forEach { parameter ->
                        buildMethods += "|\t\t${parameter.valOrVar} ${parameter.propertyName}: " +
                                "${parameter.propertyType}\n"
                    }
                buildMethods += "|\t method" + method.methodStatements + "\n-----------------\n"
            }
            println(buildMethods)

        }*/

        // TODO - this is buggy as hell I need to fix it smh
        if (scanner.mapClassViewNodes.size > 0) {
            println("DETECTED LAMBDA ELEMENTS IN PROJECT: ")
            scanner.mapClassViewNodes.forEach { className, digraph ->
                println(className)
                digraph.viewNodes.forEach { bucket, children ->
                    val nodeLevel = bucket.level
                    var viewNode = "$nodeLevel \t"

                    children.forEachIndexed { index, node ->
                        viewNode += if (index < children.size) {
                            "${node.uiNode} -> "
                        } else "${node.uiNode}\n"

                    }
                    println(viewNode)
                }
            }
        }

        if (scanner.detectedUIControls.size > 0) {
            println("DERIVING INPUTS: ")
            scanner.detectedUIControls.forEach {
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
            scanner.parseAST(fileText)
        }
    }

    // TODO: Either use regex or better parsing
    // filter files for only Views and Controllers
    private fun filterFiles(fileText: String): Boolean {
        return !fileText.contains("ApplicationTest()")
                && !fileText.contains("src/test")
                && !fileText.contains("@Test")
                && !fileText.contains("class Styles")
    }

    fun askUserDialog() {
        view.overlay.fade(Duration.millis(2000.0), .5)
        find(Dialog::class).openModal()
    }
}