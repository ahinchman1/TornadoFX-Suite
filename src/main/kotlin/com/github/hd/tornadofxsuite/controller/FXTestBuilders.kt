package com.github.hd.tornadofxsuite.controller

import com.github.ast.parser.Digraph
import com.github.ast.parser.TestClassInfo
import com.github.ast.parser.UINode
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
        // get node referencing here
        digraph.breadthFirstSearch(digraph.root, node)

        return "\t@Test\n" +
                "\tfun ${node.uiNode}ClickTest() { \n" +
                "\t$view()\n\n" +
                "" +
                "\t\t\n" +
                "}\n\n"
    }

    private fun buildButtonTest(): String {
        return ""
    }

    /**
     * Stage 1: If there's an argument in the textfield, we may assume that it may be filled
     * on population. Otherwise, we check if there is nothing in it.
     *
     * Further considerations:  Implement model checks. Will need to provide support for models
     */
    private fun buildTextFieldTest(node: UINode): String {
        return if (node.nodeTree.has("args")) {
            """

                Assert.assertNotNull(${node.uiNode}Node.text)
                Assert.assertTrue(!${node.uiNode}Node.isEmpty())

            """.trimIndent()
        } else {
            """

                Assert.assertTrue(${node.uiNode}Node.isEmpty())

            """.trimIndent()
        }
    }

    private fun buildToggleButtonTest(): String {
        return ""
    }

}