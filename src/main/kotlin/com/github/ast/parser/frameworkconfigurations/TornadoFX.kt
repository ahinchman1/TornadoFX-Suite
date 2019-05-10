package com.github.ast.parser.frameworkconfigurations

import com.github.ast.parser.*
import com.github.ast.parser.nodebreakdown.Digraph
import com.github.ast.parser.nodebreakdown.UINode
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.util.*

/**
 * TornadoFX-specific configurations
 */

// TODO - Add Model support
enum class MODELS {
    ItemViewModel, ViewModel
}

enum class COMPONENTS {
    View, Fragment
}

enum class NODES {
    BorderPane, ListView, TableView, VBox, HBox,
    DataGrid, ImageView, GridPane, Row, Form, FieldSet,
    TextField, Button, DateField, ComboButton, ComboForm,
    CheckBox, Paginator, PasswordField, TreeView, TabView
}

/**
 * UINode Inputs to interact with for testing. Will add over time.
 */
enum class INPUTS {
    TextField, Button, Form
}

/**
 * TornadoFX specific:
 *    Component breakdown meant to be saved and used for test generation
 */
fun KParserImpl.saveComponentBreakdown(
        superClass: String,
        className: String
) {
    val currentTFXView = TornadoFXView()

    if (superClass == COMPONENTS.Fragment.name || superClass == COMPONENTS.View.name) {
        currentTFXView.view = className
        currentTFXView.type = superClass
    }

    if (!currentTFXView.type.isNullOrEmpty()) {
        views[className] = currentTFXView
        // TODO need to create a contravariant function to allow this to happen
        // parser.views.add(currentTFXView)
    }
}

/**
 * TornadoFX specific:
 *    Detects TornadoFX Scopes for Views
 */
fun KParserImpl.detectScopes(
        isolatedName: String,
        className: String,
        node: JsonObject
) {
    if (isolatedName == "scope") {
        views[className]?.scope = node.expr().rhs().ref().getType()

        // TODO need to create a contravariant function to allow this to happen
        // val viewClass = parser.views.find { view -> view.viewClass == className }
        // viewClass.scope = node.expr().rhs().ref().getType()
    }
}

/**
 * TornadoFX specific:
 *    Detects TornadoFX Scopes for Views
 */
fun KParserImpl.detectRoot(
        isolatedName: String,
        className: String,
        node: JsonObject
) {
    if (isolatedName == "root") {
        viewImports[className] = saveViewImport()
        println("DETECTION ORDER")
        detectLambdaControls(node, className, LinkedList())
        println("END OF DETECTION ORDER")
    }
}

/**
 * TornadoFX specific:
 *    Detects TornadoFX View component DSLs which builds a digraph representation
 */
fun KParserImpl.detectLambdaControls(
                         node: JsonObject,
                         className: String,
                         nodeHier: LinkedList<String>,
                         nodeLevel: Int = 0
) {

    val root = node.expr()

    if (root.has("lambda")) {
        val rootName = root.asJsonObject.expr().name()
        nodeHier.addLast(rootName)
        println("$nodeLevel - $rootName")

        /**
         * Create Digraph if the class is new, otherwise, add node to the existing digraph.
         */
        val graphNode = UINode(rootName, nodeLevel, root, ArrayList())
        if (mapClassViewNodes.contains(className)) {
            mapClassViewNodes[className]?.addNode(graphNode)
        } else {
            val digraph = Digraph()
            digraph.addNode(graphNode)
            mapClassViewNodes[className] = digraph
        }

        /**
         * Add an edge (a child node) to the parent node level if there is a parent
         */
        val parentLevel = nodeLevel - 1
        if (parentLevel >= 0) {
            // find the parent node by index
            mapClassViewNodes[className]?.findLastElementWithParentLevel(parentLevel)?.let {
                mapClassViewNodes[className]?.addEdge(it, graphNode)
            }
        }

        // TornadoFX specific
        addControls<INPUTS>(graphNode, className)

        // get elements in lambda
        val lambda = root.asJsonObject.lambda()
        val elements: JsonArray = lambda.func().block().stmts()

        elements.forEach {
            detectLambdaControls(it.asJsonObject, className, nodeHier, nodeLevel + 1)
        }
    }
}