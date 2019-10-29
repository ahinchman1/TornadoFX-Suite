package com.github.ast.parser.nodebreakdown

import com.github.ast.parser.frameworkconfigurations.TornadoFXView
import com.github.ast.parser.nodebreakdown.digraph.UINodeDigraph
import java.util.*

typealias MapKClassTo<T> = HashMap<String, T>
typealias MapKClassToDigraph<T> = HashMap<String, T>

data class ClassBreakDown(val className: String,
                          val parentClasses: ArrayList<String>,
                          val properties: ArrayList<Property>,
                          val methods: ArrayList<Method>)

// keep a list of node properties expected to be changed?
data class Property(val valOrVar: String,
                    val name: String,
                    val type: String)

data class Method(val name: String = "",
                  val parameters: ArrayList<Property>,
                  val returnType: String = "Unit",
                  val methodStatements: ArrayList<String>,
                  val viewNodesAffected: ArrayList<String>)

data class TestClassInfo(val className: String,
                         val viewImport: String,
                         val detectedUIControls: ArrayList<UINode>,
                         val viewHierachy: UINodeDigraph,
                         val tfxView: TornadoFXView) {
    fun getNodeChildren(node: UINode): HashSet<Node.UINode> = viewHierachy.getChildren(node)
}













