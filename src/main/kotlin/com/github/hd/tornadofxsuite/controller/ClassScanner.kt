package com.github.hd.tornadofxsuite.controller

import com.github.hd.tornadofxsuite.model.ClassBreakDown
import com.github.hd.tornadofxsuite.model.Fields
import com.github.hd.tornadofxsuite.model.Methods
import com.github.hd.tornadofxsuite.model.Parameters
import kastree.ast.Node
import kastree.ast.psi.Parser
import tornadofx.*


class ClassScanner: Controller() {

    lateinit var className: String
    var classConstructors = ArrayList<Parameters>()
    var classMembers = ArrayList<Methods>()

     fun parseAST(textFile: String): ClassBreakDown {
        val file = Parser.parseFile(textFile)
        println(file)

         // class name
         className = (file.decls[0] as Node.Decl.Structured).name


         // class constructors
         (file.decls[0] as Node.Decl.Structured).primaryConstructor?.params?.forEach {
            classConstructors.add(detectParameters(it))
         }

         // class fields
         // TODO: find what class global fields look like

         // class members
         (file.decls[0] as Node.Decl.Structured).members.forEach {
             classMembers.add(detectMembers(it as Node.Decl.Func))
         }

         return ClassBreakDown(className, "public", classConstructors,  classMembers)
    }

    private fun detectParameters(node: Node.Decl.Func.Param): Parameters {
        return Parameters(node.name, (node.type.ref as Node.TypeRef.Simple).pieces[0].name)
    }

    private fun detectMembers(node: Node.Decl.Func): Methods {
        val parameters = ArrayList<Fields>()
        node.params.forEach {
            detectParameters(it)
        }

        return Methods(node.name, "public", parameters, "void")
    }
}