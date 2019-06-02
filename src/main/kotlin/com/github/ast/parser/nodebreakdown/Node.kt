package com.github.ast.parser.nodebreakdown

import com.github.ast.parser.nodebreakdown.digraph.Digraph
import com.google.gson.JsonObject
import java.util.ArrayList

typealias UINode = Node.UINode

typealias UIFunctionComposition = Node.UIFunctionComposition

sealed class Node(
        val level: Int
) {

    /**
     * A UI Node type containing information for elements in a view/fragment intended for writing tests and
     * mapping further compositional analysis.
     *
     * @param nodeType the kind of element.
     * @param level determines how deep a node sits within a view hierarchy.
     * @param nodeChildren a way to uniquely identify the node by its children.
     * @param functionComposition a compositional map of functions fired from interaction of this node.
     * @param propertyAssignment when a value is assigned to a val or var, used for identifying nodes in function mapping.
     * @param associatedFunctions any functions that may affect a state of this node or its properties.
     */
    data class UINode(
            val nodeType: String,
            val uiLevel: Int,
            val nodeChildren: JsonObject,
            val propertyAssignment: String,
            val functionComposition: Digraph<UIFunctionComposition>,
            val associatedFunctions: ArrayList<String>
    ): Node(uiLevel)

    /**
     * A compositional map of functions fired from interaction with a particular [UINode]
     *
     * @param className the class where the method resides.
     * @param method a [Method] used as an identifier.
     * @param level the order of the function called from the starting point.
     */
    data class UIFunctionComposition(
            val className: String,
            val method: Method?,
            // the order of the method called from the base point of the original method
            val functionLevel: Int
    ): Node(functionLevel)
}