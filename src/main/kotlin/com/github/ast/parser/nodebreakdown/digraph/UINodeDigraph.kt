package com.github.ast.parser.nodebreakdown.digraph

import com.github.ast.parser.nodebreakdown.UINode
import com.google.gson.JsonObject
import java.util.*

/**
 * Store view hierarchy in the form of a directed graph
 */
class UINodeDigraph: Digraph<UINode>() {

    /**
     * Iterate through the linked list and insert the latest node level into a
     * fixed size array (since we know what level we're looking for).
     *
     * If the node level in the trace is greater than the destination
     * don't worry about it
     **/
    override fun getDirectPath(nodeLevel: Int, trace: Queue<UINode>): MutableList<UINode> {
        val nodePath = MutableList(nodeLevel + 1) {
            UINode(
                    nodeType = "",
                    uiLevel = 0,
                    nodeChildren = JsonObject(),
                    propertyAssignment = "",
                    functionComposition = FunctionDigraph(),
                    associatedFunctions = ArrayList()
            )
        }

        trace.iterator().forEach { node ->
            if (node.level <= nodeLevel) {
                nodePath[node.level] =  node
            }
        }

        return nodePath
    }

    /**
     * Get View Node by index
     **/
    override fun getElementByIndex(index: Int): UINode = (viewNodes.keys).toTypedArray()[index]

    fun getNodeByPropertyAssignment(propertyAssignment: String): UINode? {
        if (propertyAssignment.isBlank()) return null
        return getChildren(root).firstOrNull { node->
            node.propertyAssignment == propertyAssignment
        }
    }
}
