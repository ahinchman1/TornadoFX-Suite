package com.github.hd.tornadofxsuite.controller

import com.github.ast.parser.Digraph
import com.github.ast.parser.TestClassInfo
import com.github.ast.parser.UINode
import tornadofx.*
import java.io.File
import java.util.ArrayList
import java.util.HashMap
import kotlin.random.Random

class FXTestBuilders : Controller() {

    // TODO - may need to refactor with the enums definitions in TornadoFX.kt, enums are redundant
    private val controlDictionary = hashMapOf(
            "form" to "Form",
            "textfield" to "Textfield",
            "button" to "Button"
    )

    private val controlHashMap = hashMapOf<UINode, String>()

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

    /**
     * Breakdown controls and class information to write test files for every relevant view/fragment
     */
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

    /**
     * Writes the test file.
     * TODO - save file into the proper project itself, create a method for creating a test folder?
     */
    private fun writeTestFile(classInfo: TestClassInfo) {

        val controls = createControls(classInfo)

        var file = """
            import javafx.scene.Node
            import javafx.scene.control.Button
            import javafx.scene.control.TextField
            import org.junit.Test
            import tornadofx.*
            import kotlin.reflect.KClass
            import javafx.scene.Parent
            import javafx.scene.Scene
            import javafx.stage.Stage
            import junit.framework.Assert.*
            import org.junit.Before
            import org.testfx.api.FxToolkit
            import org.testfx.framework.junit.ApplicationTest
            import ${classInfo.viewImport}

            data class Control(val type: KClass<out Node>,
                   val id: String,
                   val isAssigned: Boolean = false)

            class TestView: View() {
                override val root = vbox()
            }

            class ${classInfo.className}Test: ApplicationTest {
        """.trimIndent() +
                setup() +
                controls +
                dynamicallyAddIds()

        // Step 3: According to a dictionary of tests, write combinations
        classInfo.detectedUIControls.forEach {control ->
            file += testStub(classInfo.className, classInfo.mappedViewNodes, control)
        }

        file += "}"

        File("${classInfo.className}Test.kt")
                .printWriter().use {out -> out.println(file)}

    }

    /**
     * The @Before test method of the TestFX application.  Saves desired UI controls as
     * member variables to use throughout the rest of the testing class
     */
    private fun setup(): String {

        var setup = ""

        controlHashMap.forEach {
            val formVar = when (it.key.uiNode) {
                "form" -> "lateinit var ${it.value}Form: Form\n"
                "button" -> "lateinit var ${it.value}Button: Button\n"
                "textfield" -> "lateinit var ${it.value}TextField: TextField\n"
                else -> ""
            }
            setup += formVar
        }

        setup +=  """

            @Before
            fun setup() {
                primaryStage = FxToolkit.registerPrimaryStage()
                val fragment = find<Editor>(CatScheduleScope())

                view.root.add(fragment.root)

                addAllIdsToDescendents(view.root)

                interact {
                    primaryStage.scene = Scene(view.root)
                    primaryStage.show()
                    primaryStage.toFront()
        """.trimIndent()

        controlHashMap.forEach {
            val lookup = when (it.key.uiNode) {
                "form" -> "${it.value}Form = form(view.root).lookup(#${it.value}).query()\n"
                "button" -> "${it.value}Button = form(view.root).lookup(#${it.value}).query()\n"
                "textfield" -> "${it.value}TextField = form(view.root).lookup(#${it.value}).query()\n"
                else -> ""
            }
            setup += lookup
        }

        setup += "\t\t}\n\t}\n\n"

        return setup
    }

    /**
     * Generates random ids for detected UI nodes to attach to test application view/fragment
     * at runtime.
     */
    private fun createControls(classInfo: TestClassInfo): String {
        var controlList = "\tprivate val listOfControls = listOf(\n"

        classInfo.detectedUIControls.forEach {
            val id = randomString()
            controlList += "\t\tControl(" + controlDictionary[it.uiNode] +
                    "::class, " + id + ")\n"
            controlHashMap[it] = id
        }

        return "$controlList\n\t)"
    }

    /**
     * Code needed to attached generated random ids to respective UI elements at runtime.
     */
    private fun dynamicallyAddIds(): String {
        return """
            private fun addAllIdsToDescendents(parent: Parent) {
                for (node in parent.childrenUnmodifiable) {
                    checkToAttachNodeId(node)
                    if (node is Parent) {
                        addAllIdsToDescendents(node)
                    }
                }
            }

            private fun checkToAttachNodeId(node: Node) {
                listOfControls.forEach { control ->
                    if (control.type.isInstance(node) &&
                            !control.isAssigned &&
                            node.id == null) {
                        node.id = control.id
                        control.isAssigned
                    }
                }
            }
        """.trimIndent()
    }

    /**
     * Test stubs written for every control detected. TestFX matchers and unit testing here.
     */
    private fun testStub(view: String, digraph: Digraph, node: UINode): String {
        val nodePath = digraph.depthFirstSearch(digraph.root, node)
        val performAndExpectation = performAction(node)

        // TODO - add scopes to class breakdown for fragments. Need scope support.
        // TODO - write expected behavior for testing forms, buttons, textfields
        // TODO - take out reference node with the id that the node will actually use

        return """

                @Test
                fun ${node.uiNode}ClickTest() {
                    val ${node.uiNode}Node = ${referenceNode(nodePath)}

                    ${performAndExpectation.first}
                    ${performAndExpectation.second}
                }
        """.trimIndent()
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

    private fun performAction(node: UINode): Pair<String, String> {
        return when (node.uiNode) {
            "form" -> Pair("", "")
            "textfield" -> Pair(buildTextFieldTest(node), "")
            "button" -> Pair("", "")
            else -> TODO()
        }
    }

    private fun buildFormTest(): String {
        return ""
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
            // this may be potentially flappy if a model parameter returns an empty
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

    /**
     * Stage 1: Click button.
     *
     * Further considerations: Check the associated action and trace through to see what other
     * nodes may be affected
     */
    private fun buildButtonTest(node: UINode): String {
        return """
            Assert
        """.trimIndent()
    }

    private val charPool = ('0'..'z').toList().toTypedArray()

    private fun randomString() = (1..7)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

}