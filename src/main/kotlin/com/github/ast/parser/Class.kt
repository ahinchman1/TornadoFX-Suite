package com.github.ast.parser

import com.google.gson.JsonObject
import java.util.*

data class ClassBreakDown(val className: String,
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
                         val mappedViewNodes: Digraph)


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

    // use bfs for nodes since trees are probably wider than they are deep
    fun breadthFirstSearch(source: UINode, destination: UINode): Boolean {
        if (!viewNodes.containsKey(source) || !viewNodes.contains(destination)) {
            return false
        }
        val queue = LinkedList<UINode>()
        println("EXECUTING SEARCH")
        queue.addLast(source)
        println(queue.toString())
        return breadthFirstSearch(queue, destination)
    }

    /*private fun breadthFirstSearchCorrect(source: UINode, destination: UINode): Boolean {
        if (viewNodes.containsKey(destination)) {
            val visited = HashMap<UINode, VisitStatus>()
            val path = LinkedList<UINode>() // path source
            val queue = LinkedList<UINode>() // to trace through

            // mark current node as visited and enqueue it
            visited[source] = VisitStatus.VISITING
            path.add(source)

            var current = source
            if (current == destination) return true


            viewNodes[current]?.iterator()?.forEach {childNodes ->

            }
        }

    }*/

    private fun breadthFirstSearch(
            queue: LinkedList<UINode>,
            destination: UINode
    ): Boolean {
        val visited = HashMap<UINode, VisitStatus>()
        while (queue.isNotEmpty()) {
            val current = queue.remove()

            visited[current] = VisitStatus.VISITING
            if (current == destination) return true

            viewNodes[current]?.iterator()?.forEach { neighbor ->
                if (visited.containsKey(neighbor)) {
                    if (visited[neighbor] == VisitStatus.UNVISITED) {
                        println(neighbor.uiNode)
                        queue.addLast(neighbor)
                        println("Adding ${neighbor.uiNode}")
                        println(queue.toString())
                    }
                } else {
                    queue.addLast(neighbor)
                    println("Adding ${neighbor.uiNode}")
                    println(queue.toString())
                }
            }
            visited[current] = VisitStatus.VISITED
        }
        return false
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










