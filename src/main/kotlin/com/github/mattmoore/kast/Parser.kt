package com.github.mattmoore.kast

val parse = { source: String ->
    val kAst = kastree.ast.psi.Parser.parseFile(source)
    val classes = kAst.decls.asSequence()
        .filterIsInstance<kastree.ast.Node.Decl.Structured>()
        .map { classNode(it) }
        .toList()
    val properties = kAst.decls.asSequence()
        .filterIsInstance<kastree.ast.Node.Decl.Property>()
        .map { propertyNode(it) as Node }
        .toList()
    val functions = kAst.decls.asSequence()
        .filterIsInstance<kastree.ast.Node.Decl.Func>()
        .map { functionNode(it) }
        .toList()
    println(AbstractSyntaxTree(listOf(classes, properties, functions).flatten()))
    AbstractSyntaxTree(listOf(classes, properties, functions).flatten())
}

val classNode = { node: kastree.ast.Node.Decl.Structured ->
    val methods = node.members.asSequence()
        .filter { it is kastree.ast.Node.Decl.Func }
        .map { functionNode(it as kastree.ast.Node.Decl.Func) }
        .toList()
    val properties = node.members.asSequence()
        .filter { it is kastree.ast.Node.Decl.Property }
        .map { propertyNode(it as kastree.ast.Node.Decl.Property) }
        .toList()
    Class(name = node.name, properties = properties, methods = methods)
}

val functionNode = { node: kastree.ast.Node.Decl.Func ->
    Function(
        name = node.name,
        parameters = node.params.map { parameterNode(it) },
        accessLevel = AccessLevel.Public,
        returnType = (node.type?.ref as kastree.ast.Node.TypeRef.Simple).pieces.first().name
    )
}

val parameterNode = { node: kastree.ast.Node.Decl.Func.Param ->
    Parameter(
        name = node.name,
        dataType = (node.type.ref as kastree.ast.Node.TypeRef.Simple).pieces.first().name)
}

val propertyNode = { node: kastree.ast.Node.Decl.Property ->
    val name = if (node.expr is kastree.ast.Node.Expr.Call)
        ((node.expr as kastree.ast.Node.Expr.Call).expr as kastree.ast.Node.Expr.Name).name
    else
        (node.expr as kastree.ast.Node.Expr.Name).name
    Property(name = name, readOnly = node.readOnly)
}
