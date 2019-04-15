package com.github.ast.parser

import com.google.gson.JsonObject
import java.util.*

data class ClassBreakDown(val className: String,
                          val classParent: ArrayList<String>,
                          val classProperties: ArrayList<Property>,
                          val classMethods: ArrayList<Method>)

data class Property(val valOrVar: String,
                    val propertyName: String,
                    val propertyType: String)

data class Method(val name: String,
                  val parameters: ArrayList<Property>,
                  val returnType: String = "Unit",
                  val methodStatements: ArrayList<String>,
                  val viewNodesAffected: ArrayList<String>)

data class TestClassInfo(val className: String,
                         val viewImport: String,
                         val detectedUIControls: ArrayList<UINode>,
                         val mappedViewNodes: Digraph,
                         val tfxView: TornadoFXView)


data class UINode(val uiNode: String,
                  val level: Int,
                  val nodeTree: JsonObject, // how to identify the nodeTree
                  val associatedFunctions: ArrayList<String>)

enum class VisitStatus {
    UNVISITED, VISITING, VISITED
}


/**
 * Store view hierarchy tree here
 */
class Digraph {

    val viewNodes = LinkedHashMap<UINode, HashSet<UINode>>()
    lateinit var root: UINode
    var size = 0

    // add node to list
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

    // add child node
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

    fun isAdjacent(source: UINode, destination: UINode): Boolean =
            viewNodes[source]?.contains(destination) ?: false

    fun hasNode(node: UINode): Boolean = viewNodes.containsKey(node)

    fun getChildren(node: UINode): HashSet<UINode> = viewNodes[node] ?: HashSet()

    // it actually needs be looking by deepness
    fun depthFirstSearch(source: UINode, destination: UINode): Array<UINode> {
        if (!viewNodes.containsKey(source) || !viewNodes.contains(destination)) {
            return emptyArray()
        }
        val stack = Stack<UINode>()
        println("EXECUTING SEARCH")
        stack.push(source)
        return depthFirstSearch(stack, destination)
    }

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
        val nodePath = Array(nodeLevel + 1) {  UINode("", 0, JsonObject(), ArrayList()) }

        trace.iterator().forEach { node ->
            if (node.level <= nodeLevel) {
                nodePath[node.level] =  node
            }
        }

        return nodePath
    }

    fun getElementByIndex(index: Int): UINode = (viewNodes.keys).toTypedArray()[index]

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










