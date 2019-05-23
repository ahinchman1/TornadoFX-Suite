package com.github.ast.parser.nodebreakdown.digraph

import com.github.ast.parser.nodebreakdown.UIFunctionComposition
import java.util.*

/**
 * Store view hierarchy in the form of a directed graph
 */
class FunctionDigraph: Digraph<UIFunctionComposition>() {

    /**
     * Iterate through the linked list and insert the latest node level into a
     * fixed size array (since we know what level we're looking for).
     *
     * If the node level in the trace is greater than the destination
     * don't worry about it
     **/
    override fun getDirectPath(nodeLevel: Int, trace: Queue<UIFunctionComposition>): MutableList<UIFunctionComposition> {
        val nodePath = MutableList(nodeLevel + 1) {
            UIFunctionComposition(
                    className = "",
                    method = null,
                    functionLevel = 0
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
    override fun getElementByIndex(index: Int): UIFunctionComposition = (viewNodes.keys).toTypedArray()[index]
}