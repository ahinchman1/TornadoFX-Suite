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

    // add node to list
    fun addNode(node: UINode): Boolean {
        var result = false
        if (!viewNodes.containsKey(node)) {
            viewNodes[node] = HashSet()
            result = true
        }
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
        queue.addLast(source)
        return breadthFirstSearch(queue, destination)
    }

    private fun breadthFirstSearch(queue: LinkedList<UINode>, destination: UINode): Boolean {
        val visited = HashMap<UINode, VisitStatus>()
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            visited[current] = VisitStatus.VISITING
            if (current == destination) return true

            viewNodes[current]?.iterator()?.forEach { neighbor ->
                if (visited.containsKey(neighbor)) {
                    if (visited[neighbor] == VisitStatus.UNVISITED) {
                        queue.addLast(neighbor)
                    }
                } else {
                    queue.addLast(neighbor)
                }
            }
            visited[current] = VisitStatus.VISITED
        }
        return false
    }

    fun getElementByIndex(index: Int): UINode {
        return (viewNodes.keys).toTypedArray()[index]
    }

}










