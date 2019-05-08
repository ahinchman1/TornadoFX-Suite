package com.github.hd.tornadofxsuite.controller

import com.github.ast.parser.*
import tornadofx.*
import java.io.BufferedReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList

class PrintFileToConsole(val file: String, val textFile: String): FXEvent()
class OnParsingComplete(val testClassInfo: ArrayList<TestClassInfo>): FXEvent()

class FXTestGenerator: Controller() {
    private val kotlinFiles = ArrayList<File>()
    private val scanner: KParser by inject()

    /**
     * Open every file for AST parsing and send class breakdown for test generation
     */
    fun walk(path: String) {
        Files.walk(Paths.get(path)).use { allFiles ->
            allFiles.filter { path -> path.toString().endsWith(".kt") }
                    .forEach {path ->
                        val file = File(path.toUri())
                        readFiles(file, path.toUri().path)
                    }
        }
        consoleLogViewHierarchy()
        consoleLogClassBreakdown()

        val classes = breakupClasses(
                scanner.viewImports,
                scanner.mapClassViewNodes,
                scanner.detectedUIControls,
                scanner.views
        )

        fire(OnParsingComplete(classes))
    }

    /**
     * Read file and start analyzing file with AST parsing
     */
    private fun readFiles(file: File, path: String) {
        val fileText = file.bufferedReader().use(BufferedReader::readText)


        if (filterFiles(fileText)) {
            kotlinFiles.add(file)
            fire(PrintFileToConsole(file.toString(), fileText))
            scanner.parseAST(fileText, path)
        }
    }

    /**
     * Breakdown controls and class information to write test files for every relevant view/fragment
     */
    private fun breakupClasses(
            viewImports: HashMap<String, String>,
            mappedViewNodes: HashMap<String, Digraph>,
            detectedUIControls: HashMap<String, java.util.ArrayList<UINode>>,
            tfxViews: HashMap<String, TornadoFXView>
    ): java.util.ArrayList<TestClassInfo> {
        val classes = java.util.ArrayList<TestClassInfo>()

        viewImports.forEach { (className, item) ->
            // check that all items are there
            if (detectedUIControls.containsKey(className) &&
                    mappedViewNodes.containsKey(className)) {
                val uiControls = detectedUIControls[className] ?: java.util.ArrayList()
                val mappedNodes = mappedViewNodes[className] ?: Digraph()
                val tfxView = tfxViews[className] ?: TornadoFXView()

                classes.add(TestClassInfo(className, item, uiControls, mappedNodes, tfxView))
            } else println("Missing info for $className")
        }

        return classes
    }

    /**
     * Filter files for only Views and Controllers
     */
    private fun filterFiles(fileText: String): Boolean {
        return !fileText.contains("ApplicationTest()")
                && !fileText.contains("src/test")
                && !fileText.contains("@Test")
                && !fileText.contains("class Styles")
    }

    /**
     * Print entire View Hierarchy
     */
    private fun consoleLogViewHierarchy() {
        if (scanner.mapClassViewNodes.size > 0) {
            println("DETECTED LAMBDA ELEMENTS IN PROJECT: ")
            scanner.mapClassViewNodes.forEach { (className, digraph) ->
                println(className)
                digraph.viewNodes.forEach { (bucket, children) ->
                    val nodeLevel = bucket.level
                    var viewNode = "$nodeLevel \t${bucket.uiNode}"

                    children.forEachIndexed { index, node ->
                        viewNode += if (index < children.size) {
                            " -> ${node.uiNode} "
                        } else "${node.uiNode}\n"

                    }
                    println(viewNode)
                }
            }
        }

        if (scanner.detectedUIControls.size > 0) {
            println("DERIVING INPUTS: ")
            scanner.detectedUIControls.forEach(::println)
        }
    }

    /**
     * Prints class breakdown including methods
     * TODO - Finish method breakdown formatting and make it testable
     */
    fun consoleLogClassBreakdown() {
        scanner.classes.forEach { classBreakDown ->
            println("CLASS NAME: " + classBreakDown.className)
            println("CLASS PROPERTIES: ")
            classBreakDown.classProperties.forEach { property ->
                println("\t${property.propertyName}: ${property.propertyType}")
            }
            println("CLASS METHODS: ")
            var buildMethods = ""
            classBreakDown.classMethods.forEach { method ->
                buildMethods += buildMethodStatement(buildMethods, method)
            }

            println(buildMethods)
        }
    }

    /**
     * Prints method breakdown
     * TODO - Finish method breakdown formatting and make it testable
     */
    private fun buildMethodStatement(buildMethod: String, method: Method): String {
        var methodStatement = buildMethod
        methodStatement += "-----------------\n|\tname:${method.name}\n|\tparameters: "

        method.parameters.forEach { parameter ->
            methodStatement += "|\t\t${parameter.valOrVar} ${parameter.propertyName}: " +
                    "${parameter.propertyType}\n"
        }

        methodStatement += "|\t method" + method.methodStatements + "\n-----------------\n"

        return methodStatement
    }
}