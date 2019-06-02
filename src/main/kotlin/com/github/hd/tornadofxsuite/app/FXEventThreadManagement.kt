package com.github.hd.tornadofxsuite.app

import com.github.ast.parser.nodebreakdown.ClassBreakDown
import com.github.ast.parser.nodebreakdown.MapKClassTo
import com.github.ast.parser.nodebreakdown.TestClassInfo
import tornadofx.*
import java.io.File

/**
 * Sent to FX background thread for reading and AST parsing
 */
class ReadFilesRequest(
        val file: File
) : FXEvent(EventBus.RunOn.BackgroundThread)

/**
 * Once AST parsing is complete, send to the background thread to analyze class break down
 */
class MapNodesToFunctionsRequest(
        val viewTestClassInfo: MapKClassTo<TestClassInfo>,
        val classBreakDown: ArrayList<ClassBreakDown>
): FXEvent(EventBus.RunOn.BackgroundThread)

/**
 * On read file, print the text in the file to the console
 */
class PrintFileToConsole(
        val file: String,
        val textFile: String
): FXEvent()

/**
 * Once AST parsing is complete, send the results back to the Application thread
 */
class OnParsingComplete(
        val viewTestClassInfo: MapKClassTo<TestClassInfo>,
        val classBreakDown: ArrayList<ClassBreakDown>
): FXEvent()

