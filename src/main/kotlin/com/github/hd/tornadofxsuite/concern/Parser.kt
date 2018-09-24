package com.github.hd.tornadofxsuite.concern

import com.github.hd.tornadofxsuite.model.*
import com.github.hd.tornadofxsuite.model.Function

class Parser {
    fun parse(source: String) : AbstractSyntaxTree {
        val kAst = kastree.ast.psi.Parser.parseFile(source)
        val classes = kAst.decls
                .filter { it is kastree.ast.Node.Decl.Structured }
                .map { it as kastree.ast.Node.Decl.Structured }
                .map { classNode(it) }
        val properties = kAst.decls
                .filter { it is kastree.ast.Node.Decl.Property }
                .map { it as kastree.ast.Node.Decl.Property }
                .map { propertyNode(it) as Node }
        val functions = kAst.decls
                .filter { it is kastree.ast.Node.Decl.Func }
                .map { it as kastree.ast.Node.Decl.Func }
                .map { functionNode(it) }
        return AbstractSyntaxTree(listOf(classes, properties, functions).flatten())
    }

    private fun classNode(node: kastree.ast.Node.Decl.Structured) : Class {
        val methods = node.members
                .filter { it is kastree.ast.Node.Decl.Func }
                .map { functionNode(it as kastree.ast.Node.Decl.Func) }
        val properties = node.members
                .filter { it is kastree.ast.Node.Decl.Property }
                .map { propertyNode(it as kastree.ast.Node.Decl.Property) }
        return Class(name = node.name, properties = properties, methods = methods)
    }

    private fun functionNode(node: kastree.ast.Node.Decl.Func) : Function {
        return Function(
                name = node.name,
                parameters = node.params.map { parameterNode(it) },
                accessLevel = AccessLevel.Public,
                returnType = (node.type?.ref as kastree.ast.Node.TypeRef.Simple).pieces.first().name
        )
    }

    private fun parameterNode(node: kastree.ast.Node.Decl.Func.Param) : Parameter {
        return Parameter(name = node.name, dataType = (node.type.ref as kastree.ast.Node.TypeRef.Simple).pieces.first().name)
    }

    private fun propertyNode(node: kastree.ast.Node.Decl.Property) : com.github.hd.tornadofxsuite.model.Property {
        val name = if (node.expr is kastree.ast.Node.Expr.Call)
            ((node.expr as kastree.ast.Node.Expr.Call).expr as kastree.ast.Node.Expr.Name).name
        else
            (node.expr as kastree.ast.Node.Expr.Name).name
        return Property(name = name, readOnly = node.readOnly)
    }
}