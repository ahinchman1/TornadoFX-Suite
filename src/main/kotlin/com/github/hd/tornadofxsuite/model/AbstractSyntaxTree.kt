package com.github.hd.tornadofxsuite.model

enum class NodeType {
    Function,
    Class,
    Parameter,
    Property,
}

enum class AccessLevel {
    Public,
    Private,
    Protected,
}

class AbstractSyntaxTree(val nodes: List<Node>) {
    fun functions() : List<Node> {
        return this.nodes.filter { it.nodeType == NodeType.Function }
    }

    fun classes() : List<Node> {
        return this.nodes.filter { it.nodeType == NodeType.Class }
    }

    fun properties() : List<Node> {
        return this.nodes.filter { it.nodeType == NodeType.Property }
    }
}

interface Node {
    val name: String
    val nodeType: NodeType
}

data class Class(
    override val name: String,
    override val nodeType: NodeType = NodeType.Class,
    val properties: List<Node>,
    val methods: List<Node>
) : Node

data class Property(
    override val name: String,
    override val nodeType: NodeType = NodeType.Property,
    val readOnly: Boolean = true
) : Node

data class Function(
    override val name: String,
    override val nodeType: NodeType = NodeType.Function,
    val accessLevel: AccessLevel = AccessLevel.Public,
    val returnType: String,
    val parameters: List<Parameter>
) : Node

data class Parameter(
    override val name: String,
    override val nodeType: NodeType = NodeType.Parameter,
    val dataType: String
) : Node
