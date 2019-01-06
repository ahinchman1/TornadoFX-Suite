package com.github.ast.parser

import javafx.scene.Node
import java.util.*

data class ClassBreakDown(val className: String,
                          val classProperties: ArrayList<Property>,
                          val classMethods: ArrayList<Method>)

data class Property(val propertyName: String,
                    val propertyType: String)

data class Method(val methodName: String,
                  val parameters: ArrayList<Property>,
                  val returnType: String = "Unit")

// I'm actually not sure if it's beneficial to keep an index of the path for a directed graph
// but I'mma put it in here anyway
data class UINode(val uiNode: Node,
                val nodeLevel: Int,
                val path: ArrayList<String>,
                val associatedFunctions: ArrayList<String>)


// TODO - Rethink behavior for this since there's an architecture that matters
/**
 * Store view hierarchy tree here
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

    fun getChildren(node: UINode): HashSet<UINode> = edges[node] ?: HashSet()

    // use bfs for nodes since trees are probably wider than they are deep

}










