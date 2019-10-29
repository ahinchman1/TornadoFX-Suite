package com.github.ast.parser.nodebreakdown.digraph

import com.github.ast.parser.nodebreakdown.Node
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
abstract class Digraph<T: Node> {

    val viewNodes = LinkedHashMap<T, HashSet<T>>()
    lateinit var root: T
    var size: Int = 0

    /**
     * Add UI Node to List
     */
    fun addNode(node: T): Boolean {
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
    fun removeNode(node: T): Boolean {
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
    fun addEdge(source: T, destination: T): Boolean {
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
    fun removeEdge(source: T, destination: T): Boolean {
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
    fun isAdjacent(source: T, destination: T): Boolean =
            viewNodes[source]?.contains(destination) ?: false

    /**
     * Check if a Node exists in the view hierarchy
     */
    fun hasNode(node: T): Boolean = viewNodes.containsKey(node)

    /**
     * Get Node Children in Digraph Form
     */
    fun getChildren(node: T): HashSet<T> = viewNodes[node] ?: HashSet()

    /**
     * Searches for node depth first
     */
    fun depthFirstSearch(source: T, destination: T): MutableList<T> {
        if (!viewNodes.containsKey(source) || !viewNodes.contains(destination)) {
            return mutableListOf()
        }
        val stack = Stack<T>()
        println("EXECUTING SEARCH")
        stack.push(source)
        return depthFirstSearch(stack, destination)
    }

    /**
     * Searches for node depth first recursive
     */
    private fun depthFirstSearch(stack: Stack<T>, destination: T): MutableList<T> {
        val visited = HashMap<T, VisitStatus>().withDefault { VisitStatus.UNVISITED }
        val path = LinkedList<T>()

        while(stack.isNotEmpty()) {
            val current = stack.pop()
            path.add(current)

            visited[current] = VisitStatus.VISITING
            if (current == destination) return getDirectPath(destination.level, path)

            viewNodes[current]?.iterator()?.forEach { child ->
                if (visited.getValue(child) == VisitStatus.UNVISITED){
                    stack.push(child)
                }
            }
            visited[current] = VisitStatus.VISITED
        }

        return mutableListOf()
    }

    /**
     * Iterate through the linked list and insert the latest node level into a
     * fixed size array (since we know what level we're looking for).
     *
     * If the node level in the trace is greater than the destination
     * don't worry about it
     **/
    abstract fun getDirectPath(nodeLevel: Int, trace: Queue<T>): MutableList<T>

    /**
     * Get View Node by index
     **/
    abstract fun getElementByIndex(index: Int): T

    /**
     * Find child-most element at level parentLevel
     **/
    fun findLastElementWithParentLevel(parentLevel: Int): T {
        var lastNode = root

        viewNodes.keys.forEach { node ->
            if (node.level == parentLevel) {
                lastNode = node
            }
        }

        return lastNode
    }
}
