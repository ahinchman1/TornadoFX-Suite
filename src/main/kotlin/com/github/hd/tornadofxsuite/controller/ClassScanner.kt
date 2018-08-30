package com.github.hd.tornadofxsuite.controller

import com.github.hd.tornadofxsuite.model.*
import com.github.hd.tornadofxsuite.model.Function
import kastree.ast.Node
import kastree.ast.psi.Parser
import tornadofx.*


class ClassScanner: Controller() {

    var classBreakDown = ArrayList<ClassBreakDown>()
    var classAccessLevel = "PUBLIC" // TODO re-examine class access levels
    var classProperties = ArrayList<Property>()
    var classDIs = ArrayList<DependencyInjection>()
    var classFunctions = ArrayList<Function>()


     fun parseAST(textFile: String) {
        val file = Parser.parseFile(textFile)
        println(file)

         // class name
         var className = parseName(file)

         // detect all classes in a file.
         file.decls.forEachIndexed { index: Int, decl: Node.Decl ->
             println("$index: $decl")
             val thisFile = (decl as Node.Decl.Structured)
             className = thisFile.name
             thisFile.members.forEach {
                when (it) {
                    is Node.Decl.Property -> captureClassProperty(it)
                    is Node.Decl.Func -> captureFunction(it)
                }
             }

             classBreakDown.add(ClassBreakDown(className, classAccessLevel, classProperties, classDIs, classFunctions))
         }
    }

    fun parseName(file: Node.File): String {
        return (file.decls[0] as Node.Decl.Structured).name
    }

    private fun captureClassProperty(property: Node.Decl.Property) {
        val varsName = (property.vars[0] as Node.Decl.Property.Var)
        var accessLevel = "PUBLIC"
        if (property.mods.isNotEmpty()) {
            accessLevel = (property.mods[0] as Node.Modifier.Lit).keyword.name
        }

        // determine whether the property is a class variable or a dependency injection
        if (property.delegated) {
            val diDependency = ((varsName.type as Node.Type).ref as Node.TypeRef.Simple).pieces[0].name
            classDIs.add(DependencyInjection(varsName.name, accessLevel, diDependency))
        } else {
            val type = property.vars.toString()
            val value = (property.vars[0] as Node.Decl.Property.Var).name
            classProperties.add(Property(varsName.name, accessLevel, type, value))
        }
    }

    private fun captureFunction(function: Node.Decl.Func) {
        var accessLevel = "PUBLIC"
        val functionParameters = ArrayList<Parameters>()

        if (function.mods.isNotEmpty()) {
            accessLevel = (function.mods[0] as Node.Modifier.Lit).keyword.name ?: "PUBLIC"
        }
        function.params.forEach {
            functionParameters.add(Parameters(it.name, (it.type.ref as Node.TypeRef.Simple).pieces[0].name))
        }

        // TODO don't trust that last parameter lol that will need to be re-examined again
        classFunctions.add(Function(function.name, accessLevel, functionParameters, function.type.toString()))
    }

}