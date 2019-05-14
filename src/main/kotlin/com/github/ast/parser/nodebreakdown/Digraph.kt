package com.github.ast.parser.nodebreakdown

import com.google.gson.JsonObject
import java.util.*

/**
 * Marks nodes during traversal of [Digraph] searches
 */
enum class VisitStatus {
    UNVISITED, VISITING, VISITED
}


/**
 * Store view hierarchy in the form of a directed graph
 */
class Digraph {

    val viewNodes = LinkedHashMap<UINode, HashSet<UINode>>()
    lateinit var root: UINode
    var size = 0

    /**
     * Add UI Node to List
     */
    fun addNode(node: UINode): Boolean {
        var result = false
        if (viewNodes.isEmpty()) {
            root = node
        }

        if (!viewNodes.containsKey(node)) {
            viewNodes[node] = HashSet()
            result = true
        }
        size++
        return result
    }

    /**
     * Remove UI Node from List
     */
    fun removeNode(node: UINode): Boolean {
        var result = false
        if (viewNodes.containsKey(node)) {
            viewNodes.remove(node)
            for (entry in viewNodes.entries) {
                if (entry.value.contains(node)) {
                    viewNodes[entry.key]?.remove(node)
                }
            }
            result = true
        }
        return result
    }

    /**
     * Add child Node to Node
     */
    fun addEdge(source: UINode, destination: UINode): Boolean {
        var result = false
        if (viewNodes.containsKey(source)) {
            val getSource = viewNodes[source]
            if (getSource != null && !getSource.contains(destination)) {
                getSource.add(destination)
                result = true
            }
        }
        return result
    }

    /**
     * Remove child Node from Node
     */
    fun removeEdge(source: UINode, destination: UINode): Boolean {
        var result = false
        if (viewNodes.containsKey(source)) {
            val getSource = viewNodes[source]
            if (getSource != null && getSource.contains(destination)) {
                getSource.remove(destination)
                result = true
            }
        }
        return result
    }

    /**
     * Check if destination Node is a child of source Node
     */
    fun isAdjacent(source: UINode, destination: UINode): Boolean =
            viewNodes[source]?.contains(destination) ?: false

    /**
     * Check if a Node exists in the view hierarchy
     */
    fun hasNode(node: UINode): Boolean = viewNodes.containsKey(node)

    fun getChildren(node: UINode): HashSet<UINode> = viewNodes[node] ?: HashSet()

    /**
     * Searches for node depth first
     */
    fun depthFirstSearch(source: UINode, destination: UINode): Array<UINode> {
        if (!viewNodes.containsKey(source) || !viewNodes.contains(destination)) {
            return emptyArray()
        }
        val stack = Stack<UINode>()
        println("EXECUTING SEARCH")
        stack.push(source)
        return depthFirstSearch(stack, destination)
    }

    /**
     * Searches for node depth first recursive
     */
    private fun depthFirstSearch(stack: Stack<UINode>, destination: UINode): Array<UINode> {
        val visited = HashMap<UINode, VisitStatus>()
        val path = LinkedList<UINode>()

        while(stack.isNotEmpty()) {
            val current = stack.pop()
            path.add(current)

            visited[current] = VisitStatus.VISITING
            if (current == destination) return getDirectPath(destination.level, path)

            viewNodes[current]?.iterator()?.forEach { child ->
                if (visited.containsKey(child)) {
                    if (visited[child] == VisitStatus.UNVISITED) {
                        stack.push(child)
                    }
                } else {
                    stack.push(child)
                }
            }
            visited[current] = VisitStatus.VISITED
        }

        return emptyArray()
    }

    /**
     * Iterate through the linked list and insert the latest node level into a
     * fixed size array (since we know what level we're looking for).
     *
     * If the node level in the trace is greater than the destination
     * don't worry about it
     **/
    private fun getDirectPath(nodeLevel: Int, trace: Queue<UINode>): Array<UINode> {
        val nodePath = Array(nodeLevel + 1) { UINode("", 0, JsonObject(), ArrayList()) }

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
    fun getElementByIndex(index: Int): UINode = (viewNodes.keys).toTypedArray()[index]

    /**
     * Find child-most element at level parentLevel
     **/
    fun findLastElementWithParentLevel(parentLevel: Int): UINode {
        var lastNode = root

        viewNodes.keys.forEach { node ->
            if (node.level == parentLevel) {
                lastNode = node
            }
        }

        return lastNode
    }
}
