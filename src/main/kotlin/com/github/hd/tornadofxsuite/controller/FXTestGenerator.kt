package com.github.hd.tornadofxsuite.controller

import com.github.ast.parser.*
import com.github.ast.parser.frameworkconfigurations.TornadoFXView
import com.github.ast.parser.frameworkconfigurations.detectRoot
import com.github.ast.parser.frameworkconfigurations.detectScopes
import com.github.ast.parser.frameworkconfigurations.saveComponentBreakdown
import com.github.ast.parser.nodebreakdown.*
import com.github.ast.parser.nodebreakdown.digraph.UINodeDigraph
import com.github.hd.tornadofxsuite.app.OnParsingComplete
import com.github.hd.tornadofxsuite.app.PrintFileToConsole
import tornadofx.*
import java.io.BufferedReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList

class FXTestGenerator: Controller() {

    lateinit var filePath: String

    private val kotlinFiles = ArrayList<File>()
    var scanner = KParserImpl(
            "",
            KParserImpl::saveComponentBreakdown,
            HashMap(),
            KParserImpl::detectScopes,
            KParserImpl::detectRoot
    )

    /**
     * Open every file for AST parsing and send class breakdown for test generation
     */
    fun walk(path: String) {
        filePath = path

        Files.walk(Paths.get(path)).use { allFiles ->
            allFiles.filter { path -> path.toString().endsWith(".kt") }
                    .forEach { path ->
                        scanner.currentPath = path.toUri().path

                        val file = File(path.toUri())
                        readFiles(file)
                    }
        }
        consoleLogViewHierarchy()
        consoleLogClassBreakdown()

        val viewClasses = breakupClasses(
                scanner.viewImports,
                scanner.mapClassViewNodes,
                scanner.detectedUIControls,
                scanner.views
        )

        fire(OnParsingComplete(viewClasses, scanner.classes))
    }

    /**
     * Read file and start analyzing file with AST parsing
     */
    private fun readFiles(file: File) {
        val fileText = file.bufferedReader().use(BufferedReader::readText)


        if (filterFiles(fileText)) {
            kotlinFiles.add(file)
            fire(PrintFileToConsole(file.toString(), fileText))
            scanner.parseAST(fileText)
        }
    }

    /**
     * Breakdown controls and class information to write test files for every relevant view/fragment
     */
    private fun breakupClasses(
            viewImports: HashMap<String, String>,
            mappedViewNodes: HashMap<String, UINodeDigraph>,
            detectedUIControls: HashMap<String, java.util.ArrayList<UINode>>,
            tfxViews: HashMap<String, TornadoFXView>
    ): MapKClassTo<TestClassInfo> {
        val classes = MapKClassTo<TestClassInfo>()

        viewImports.forEach { (className, item) ->
            // check that all items are there
            if (detectedUIControls.containsKey(className) &&
                    mappedViewNodes.containsKey(className)) {
                val uiControls = detectedUIControls[className] ?: java.util.ArrayList()
                val mappedNodes = mappedViewNodes[className] ?: UINodeDigraph()
                val tfxView = tfxViews[className] ?: TornadoFXView()

                classes[className] = TestClassInfo(className, item, uiControls, mappedNodes, tfxView)
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
                    var viewNode = "$nodeLevel \t${bucket.nodeType}"

                    children.forEachIndexed { index, node ->
                        viewNode += if (index < children.size) {
                            " -> ${node.nodeType} "
                        } else "${node.nodeType}\n"

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
     * TODO - Continue to touch on method parsing
     */
    private fun consoleLogClassBreakdown() {
        scanner.classes.forEach { classBreakDown ->
            println("CLASS NAME: " + classBreakDown.className)
            println("CLASS PROPERTIES: ")
            classBreakDown.classProperties.forEach { property ->
                println("\t${property.propertyName}: ${property.propertyType}")
            }
            println("CLASS METHODS: ")
            var buildMethods = ""
            classBreakDown.classMethods.forEach { method ->
                buildMethods += buildMethodStatement(method)
            }

            println(buildMethods)
        }
    }

    /**
     * Prints method breakdown
     * TODO - Finish method breakdown formatting and make it testable
     */
    private fun buildMethodStatement(method: Method): String {
        var methodStatement = ""
        methodStatement += "-----------------\n|\t${method.name}("

        method.parameters.forEachIndexed { index, parameter ->
            methodStatement += when (index) {
                method.parameters.size - 1 -> "${parameter.propertyName}: ${parameter.propertyType}"
                else -> "${parameter.propertyName}: ${parameter.propertyType}, "
            }
        }

        methodStatement += ") {"

        method.methodStatements.forEach {
            methodStatement += "\n|\t\t$it"
        }

        methodStatement += "\n|\t}\n-----------------\n"

        return methodStatement
    }
}