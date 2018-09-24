package com.example.demo.controller

import com.github.hd.tornadofxsuite.model.BareBreakDown
import com.github.hd.tornadofxsuite.model.ClassProperties
import kastree.ast.Node
import kastree.ast.psi.Parser
import tornadofx.*

/*
 * TODO - AST mappings, composition.
 */
class ClassScanner: Controller() {

    var bareClasses = ArrayList<BareBreakDown>()
    var independentFunctions = ArrayList<String>()
    var bareModelClasses = ArrayList<BareBreakDown>()

    fun parseAST(textFile: String) {
        val file = Parser.parseFile(textFile)
        // store class names and their methods
        file.decls.forEach {
            when (it) {
                is Node.Decl.Structured -> breakDownClass(it, file)
                is Node.Decl.Func -> independentFunctions.add(it.name)
            }

        }
    }

    private fun breakDownClass(someClass: Node.Decl.Structured, file: Node.File) {
        val className = someClass.name
        val classProperties = ArrayList<ClassProperties>()
        var classMembers = ArrayList<String>()

        (file.decls[0] as Node.Decl.Structured).members.forEach {
            when (it) {
                is Node.Decl.Structured -> println("this is probably a companion object")
                is Node.Decl.Property -> convertToJsonProperty(it, classProperties)
                is Node.Decl.Func -> classMembers.add(it.name)
            }
        }
        bareClasses.add(BareBreakDown(className, classProperties, classMembers))
    }

    private fun convertToJsonProperty(property: Node.Decl.Property, propList: ArrayList<ClassProperties>) {
        val firstTrueBit = "Property(mods=[], readOnly=true, typeParams=[], " +
                "receiverType=null, vars=[Var(name="
        val firstFalseBit = "Property(mods=[], readOnly=false, typeParams=[], " +
                "receiverType=null, vars=[Var(name="
        val privateFirstTrueBit = "Property(mods=[Lit(keyword=PRIVATE)], " +
                "readOnly=true, typeParams=[], receiverType=null, vars=[Var(name="
        val privateFirstFalseBit = "Property(mods=[Lit(keyword=PRIVATE)], " +
                "readOnly=false, typeParams=[], receiverType=null, vars=[Var(name="
        val secondBit = "expr=Call(expr=Name(name="
        val string = property.toString()

        // it ain't stupid if it works shut up
        if (string.contains(firstTrueBit) || string.contains(secondBit) ||
                string.contains(privateFirstTrueBit) || string.contains(privateFirstFalseBit)) {
            val splitToName = when {
                string.contains(firstTrueBit) -> string.split(firstTrueBit)[1]
                string.contains(privateFirstTrueBit) -> string.split(privateFirstTrueBit)[1]
                string.contains(firstFalseBit) -> string.split(firstFalseBit)[1]
                string.contains(privateFirstFalseBit) -> string.split(privateFirstFalseBit)[1]
                else -> "" //TODO error handling?
            }

            val isolatedName = splitToName.split(",")[0]
            if (string.contains(secondBit)) {
                val splitToType = string.split(secondBit)
                var isolatedType = splitToType[1].split(")")[0]

                if (isolatedType == "inject") {
                    isolatedType = splitToType[0].split(",")[6].split("name=")[1]
                }
                propList.add(ClassProperties(isolatedName, isolatedType))
            }
            // TODO - look into TornadoFX to see if it only accepts up to 80 char in a string
            //val json = loadJsonObject("""$string""")
        }
    }
}