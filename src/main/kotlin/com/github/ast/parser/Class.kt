package com.github.ast.parser

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
                val path: ArrayList<String>,
                val associatedFunctions: ArrayList<String>)


// TODO - Rethink behavior for this since there's an architecture that matters
/**
 * Store view hierarchy tree here
 * TODO - Can Tensorflow do directional graphing?
 */
class Digraph(val edges: HashMap<UINode, HashSet<UINode>>) {

    // TODO - adding a Node can't just be anywhere, I need to modify to find the path probably?
    fun addNode(node: UINode) {
        if (!edges.containsKey(node)) {
            edges[node] = HashSet()
        } else throw AssertionError("Duplicate node $node")
    }

    fun addEdge(source: UINode, destination: UINode) {
        if (edges.containsKey(source)) {
            val getSource = edges[source]
            if (getSource != null && getSource.contains(destination)) {
                getSource.add(destination)
            }
        }
    }

    fun removeEdge(source: UINode, destination: UINode) {
        if (edges.containsKey(source)) {
            val getSource = edges[source]
            if (getSource != null && getSource.contains(destination)) {
                getSource.remove(destination)
            }
        }
    }

    fun isAdjacent(source: UINode, destination: UINode): Boolean =
            edges[source]?.contains(destination) ?: false

    fun hasNode(node: UINode): Boolean = edges.containsKey(node)

    fun getChildren(node: UINode): HashSet<UINode> = edges[node] ?: HashSet()

    // use bfs for nodes since trees are probably wider than they are deep
    fun breadthFirstSearch(source: UINode, destination: UINode): Boolean {
        if (!edges.containsKey(source) || !edges.contains(destination)) {
            return false
        }
        val queue = LinkedList<UINode>()
        queue.addLast(source)
        return breadthFirstSearch(queue, destination)
    }

    private fun breadthFirstSearch(queue: LinkedList<UINode>, destination: UINode): Boolean {
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current == destination) return true
           // TODO - finish this but perhaps I should trade out this digraph for Tensorflow instead
            // edges[current].
        }
        return true
    }
}










