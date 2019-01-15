package com.github.hd.tornadofxsuite.controller

import com.github.ast.parser.Digraph
import com.github.ast.parser.TestClassInfo
import com.github.ast.parser.UINode
import javafx.scene.control.TextField
import tornadofx.*
import java.io.File
import java.util.ArrayList
import java.util.HashMap

class FXTestBuilders : Controller() {

    // Dictionary of testing components
    fun generateTests(
            viewImports: HashMap<String, String>,
            mappedViewNodes: HashMap<String, Digraph>,
            detectedUIControls: HashMap<String, ArrayList<UINode>>
    ) {

        // Step 1: Breakup classes
        val classes = breakupClasses(viewImports, mappedViewNodes, detectedUIControls)

        // Step 2: Write each file with their imports
        for (item in classes) {
            writeTestFile(item)
        }
    }

    private fun breakupClasses(
            viewImports: HashMap<String, String>,
            mappedViewNodes: HashMap<String, Digraph>,
            detectedUIControls: HashMap<String, ArrayList<UINode>>
    ): ArrayList<TestClassInfo> {
        val classes = ArrayList<TestClassInfo>()

        viewImports.forEach {className, item ->
            // check that all items are there
            if (detectedUIControls.containsKey(className) &&
                    mappedViewNodes.containsKey(className)) {
                val uiControls = detectedUIControls[className] ?: ArrayList()
                val mappedNodes = mappedViewNodes[className] ?: Digraph()

                classes.add(TestClassInfo(className, item, uiControls, mappedNodes))
            } else println("Missing info for $className")
        }

        return classes
    }

    private fun writeTestFile(classInfo: TestClassInfo) {

        var file = "import javafx.stage.Stage\n" +
                "import org.junit.Test\n" +
                "import org.testfx.api.FxToolkit\n" +
                "import org.testfx.framework.junit.ApplicationTest\n" +
                "import tornadofx.*\n" +
                "import kotlin.test.assertEquals\n" +
                "import ${classInfo.viewImport}\n\n" +
                "class ${classInfo.className}Test {\n" +
                setup()

        // Step 3: According to a dictionary of tests, write combinations
        classInfo.detectedUIControls.forEach {control ->
            file += testStub(classInfo.className, classInfo.mappedViewNodes, control)
        }

        file += ")"

        // write string to document
        File("${classInfo.className}Test.kt").printWriter().use {out -> out.println(file)}

    }

    private fun setup(): String {
        return "\t@Before\n" +
                "\tfun setup() {\n" +
                "\t\tFxToolkit.registerPrimaryStage()\n" +
                "\t}\n\n"
    }

    private fun replicateView(): String {
        return ""
    }

    private fun testStub(view: String, digraph: Digraph, node: UINode): String {
        val nodePath = digraph.depthFirstSearch(digraph.root, node)

        return "\t@Test\n" +
                "\tfun ${node.uiNode}ClickTest() { \n" +
                "\t\tval view = $view()\n\n" +
                "\t\tval ${node.uiNode} = ${referenceNode(nodePath)}\n" + // reference node
                "\t\t\n" +
                "\t\t\n" + // perform action on node
                "\t}\n\n"
    }

    private fun referenceNode(nodePath: Array<UINode>): String {
        var nodeReference = ""

        nodePath.forEachIndexed { index, node ->
            nodeReference += if (index == 0) {
                "root"
            } else node.uiNode
        }

        return nodeReference
    }

    private fun performAction(uiNode: UINode) {
        when (uiNode.uiNode) {
            "form" -> TODO()
            "textfield" -> TODO()
            "button" -> TODO()
        }
    }

    private fun buildTextFieldTest(): String {
        return ""
    }

    private fun buildButtonTest(): String {
        return ""
    }

    private fun buildToggleButtonTest(): String {
        return ""
    }

}