package com.github.hd.tornadofxsuite.controller

import com.github.ast.parser.Digraph
import com.github.ast.parser.TestClassInfo
import com.github.ast.parser.TornadoFXView
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

    // supports one set of forms at a time per view for now
    private var form: UINode? = null

    fun generateTests(
            viewImports: HashMap<String, String>,
            mappedViewNodes: HashMap<String, Digraph>,
            detectedUIControls: HashMap<String, ArrayList<UINode>>,
            tfxViews: HashMap<String, TornadoFXView>
    ) {
        // Step 1: Breakup classes
        val classes = breakupClasses(viewImports, mappedViewNodes, detectedUIControls, tfxViews)

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
            detectedUIControls: HashMap<String, ArrayList<UINode>>,
            tfxViews: HashMap<String, TornadoFXView>
    ): ArrayList<TestClassInfo> {
        val classes = ArrayList<TestClassInfo>()

        viewImports.forEach { (className, item) ->
            // check that all items are there
            if (detectedUIControls.containsKey(className) &&
                    mappedViewNodes.containsKey(className)) {
                val uiControls = detectedUIControls[className] ?: ArrayList()
                val mappedNodes = mappedViewNodes[className] ?: Digraph()
                val tfxView = tfxViews[className] ?: TornadoFXView()

                classes.add(TestClassInfo(className, item, uiControls, mappedNodes, tfxView))
            } else println("Missing info for $className")
        }

        return classes
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
                lateinit var primaryStage: Stage
                val view = TestView()
                
        """.trimIndent() +
                setup(classInfo) +
                controls +
                dynamicallyAddIds()

        // Step 3. separate out controls by groupings i.e. forms
        if (form != null) {
            // supports one set of forms at a time per view for now
            val formControlMap = hashMapOf<UINode, String>()

            controlHashMap.forEach { (uiNode, id) ->
                isNodeInForm(classInfo.mappedViewNodes, formControlMap, uiNode, id, form as UINode)
            }

            // Step 4: According to a dictionaries of tests, write necessary combinations
            buildFormTests(formControlMap)
        }

        // Step 4: According to a dictionaries of tests, write necessary combinations
        controlHashMap.forEach { (uiNode, id) ->
            file += testIndividualNodeStub(uiNode, id)
        }

        file += "}"

        File("${classInfo.className}Test.kt")
                .printWriter().use {out -> out.println(file)}
    }

    /**
     * The @Before test method of the TestFX application.  Saves desired UI controls as
     * member variables to use throughout the rest of the testing class
     */
    private fun setup(classInfo: TestClassInfo): String {

        var setup = ""

        controlHashMap.forEach {
            if (it.key.uiNode == "form") {
                form = it.key
            }
            val formVar = "\tlateinit var ${it.value}${controlDictionary[it.value]}: ${controlDictionary[it.value]}\n"
            setup += formVar
        }

        setup +=  """

                @Before
                fun setup() {
                    primaryStage = FxToolkit.registerPrimaryStage()
                    val ${classInfo.className.toLowerCase()}${classInfo.tfxView.type} = find<${classInfo.className}>(${classInfo.tfxView.scope})

                    view.root.add(${classInfo.className.toLowerCase()}${classInfo.tfxView.type}.root)

                    addAllIdsToDescendents(view.root)

                    interact {
                        primaryStage.scene = Scene(view.root)
                        primaryStage.show()
                        primaryStage.toFront()

        """.trimIndent()

        controlHashMap.forEach {
            val lookup = when (it.key.uiNode) {
                "form" -> "\t\t\t\t${it.value}Form = from(view.root).lookup(#${it.value}).query()\n"
                "button" -> "\t\t\t\t${it.value}Button = from(view.root).lookup(#${it.value}).query()\n"
                "textfield" -> "\t\t\t\t${it.value}TextField = from(view.root).lookup(#${it.value}).query()\n"
                else -> ""
            }
            setup += lookup
        }

        setup += "\t\t\t}\n\t\t}\n"

        setup += "\t\tprivate val listOfControls = listOf("

        setup += controlHashMap.forEach {
            "\n\t\t\tControl(${it.key}::class, \"${it.value}\"),"
        }

        setup = setup.substring(0, setup.length - 1) + "\n\t\t)\n"

        return setup
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
     * If a node is in a form, node testing may interact together in a particular way.
     * Pull out of control HashMap into a new HashMap grouping for testing
     */
    private fun isNodeInForm(digraph: Digraph,
                             formControlMap: HashMap<UINode, String>,
                             nodeControl: UINode,
                             nodeId: String,
                             form: UINode) {
        val nodePath = digraph.depthFirstSearch(form, nodeControl)

        if (nodePath.isNotEmpty()) {
            controlHashMap.remove(nodeControl)
            formControlMap[nodeControl] = nodeId
        }
    }

    /**
     * Referencing a node path may be something like root.form.fieldset.textfield and so on
     */
    private fun referenceNode(nodePath: Array<UINode>): String {
        var nodeReference = ""

        nodePath.forEachIndexed { index, node ->
            nodeReference += if (index == 0) {
                "root"
            } else node.uiNode
        }

        return nodeReference
    }

    /**
     * Test individual nodes but also combinations of those interactions
     */
    private fun buildFormTests(formControls: HashMap<UINode, String>): String {
        var formTests = ""
        var hasButton = false

        formControls.forEach { (uiNode, id) ->
            if (uiNode.uiNode != "form") {
                formTests += testIndividualNodeStub(uiNode, id)
            }
            if (uiNode.uiNode == "button") hasButton = true
        }

        // TODO in model support, include permutations
        if (hasButton) {
            // input something in all textfields and clicks a button
            formTests += "\t@Test fun testInputAll() {\n"
            formControls.forEach { (node, nodeId) ->
                formTests += when (node.uiNode) {
                    "textfield" -> "\t\tclickOn($nodeId${controlDictionary[node.uiNode]}).write(\"Something\")\n"
                    "button" -> "\t\tclickOn($nodeId${controlDictionary[node.uiNode]})\n"
                    else -> ""
                }
            }
            formTests += "\t}\n\n"

            // input nothing and click button
            formTests += "\t@Test fun testEmptyForm() {\n"
            formControls.forEach { (node, nodeId) ->
                formTests += when (node.uiNode) {
                    "textfield" -> "\t\tclickOn($nodeId${controlDictionary[node.uiNode]}).write(\"\")\n"
                    "button" -> "\t\tclickOn($nodeId${controlDictionary[node.uiNode]})\n"
                    else -> ""
                }
            }
            formTests += "\t}\n\n"
        }

        return ""
    }

    /**
     * Test stubs written for every control detected. TestFX matchers and unit testing here.
     */
    private fun testIndividualNodeStub(node: UINode,
                                       nodeId: String): String {
        val performAnActionAndCheckExpectation = performIndividualAction(node, nodeId)

        // TODO - add scopes to class breakdown for fragments. Need scope support.

        return """

                @Test fun test$nodeId${node.uiNode}() {
                    $performAnActionAndCheckExpectation
                }
        """.trimIndent()
    }

    private fun performIndividualAction(node: UINode,
                                        nodeId: String): String {
        return when (node.uiNode) {
            "textfield" -> buildTextFieldTest(node, nodeId)
            "button" -> buildButtonTest(node, nodeId)
            else -> ""
        }
    }

    /**
     * Stage 1: If there's an argument in the textfield, we may assume that it may be filled
     * on population. Otherwise, we check if there is nothing in it.
     *
     * Stage 2: Input edge cases like numbers, model support validation
     *
     * Further considerations:  Implement model checks. Will need to provide support for models
     */
    private fun buildTextFieldTest(node: UINode,
                                   nodeId: String): String {

        return if (node.nodeTree.has("args")) {
            // this may be potentially flappy if a model parameter returns an empty
            """
                        assertNotNull($nodeId${node.uiNode}.text)
                        assertTrue(!$nodeId${node.uiNode}.text.isNullOrEmpty())

                        clickOn($nodeId${controlDictionary[node.uiNode]}).write("Something")
                        assertEquals("Something", $nodeId${controlDictionary[node.uiNode]}.text)

            """.trimIndent()
        } else {
            """
                        assertTrue($nodeId${node.uiNode}.text.isNullOrEmpty())

                        clickOn($nodeId${controlDictionary[node.uiNode]}).write("Something")
                        assertEquals("Something", $nodeId${controlDictionary[node.uiNode]}.text)

            """.trimIndent()
        }
    }

    /**
     * Stage 1: Click button.
     *
     * Stage 2: Determine whether there is enabling for the button, track action of button
     *
     * Further considerations: Check the associated action and trace through to see what other
     * nodes may be affected
     */
    private fun buildButtonTest(node: UINode,
                                nodeId: String): String {
        return """
            clickOn($nodeId${controlDictionary[node.uiNode]})
        """.trimIndent()
    }

    private val charPool = ('0'..'z').toList().toTypedArray()

    private fun randomString() = (1..7)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

}