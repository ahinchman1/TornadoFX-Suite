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

    var classMembers = ArrayList<String>()
    var bareClasses = ArrayList<BareBreakDown>()
    var classProperties = ArrayList<ClassProperties>()

    fun parseAST(textFile: String) {
        val file = Parser.parseFile(textFile)

        // store class names and their methods
        file.decls.forEach {
            val className = (it as Node.Decl.Structured).name

            (file.decls[0] as Node.Decl.Structured).members.forEach {
                val memberName = ""
                when (it) {
                    is Node.Decl.Structured -> println("this is probably a companion object")
                    is Node.Decl.Property -> convertToJsonProperty(it) // TODO: fill out later
                    is Node.Decl.Func -> classMembers.add(it.name)

                }
            }
            bareClasses.add(BareBreakDown(className, classProperties, classMembers))
        }
    }

    private fun convertToJsonProperty(property: Node.Decl.Property) {
        val firstTrueBit = "Property(mods=[], readOnly=true, typeParams=[], " +
                "receiverType=null, vars=[Var(name="
        val firstFalseBit = "Property(mods=[], readOnly=false, typeParams=[], " +
                "receiverType=null, vars=[Var(name="
        val privateFirstTrueBit = "Property(mods=[Lit(keyword=PRIVATE)], " +
                "readOnly=true, typeParams=[], receiverType=null, vars=[Var("
        val privateFirstFalseBit = "Property(mods=[Lit(keyword=PRIVATE)], " +
                "readOnly=false, typeParams=[], receiverType=null, vars=[Var("
        val secondBit = "expr=Call(expr=Name(name="
        val string = property.toString()

        // it ain't stupid if it works shut up
        if (string.contains(firstTrueBit) || string.contains(secondBit) ||
                string.contains(privateFirstTrueBit) || string.contains(privateFirstFalseBit)) {
            println(string)
            val splitToName = when {
                string.contains(firstTrueBit) -> string.split(firstTrueBit)[1]
                string.contains(privateFirstTrueBit) -> string.split(privateFirstTrueBit)[1]
                string.contains(firstFalseBit) -> string.split(firstFalseBit)[1]
                string.contains(privateFirstFalseBit) -> string.split(privateFirstFalseBit)[1]
                else -> return
            }

            val isolatedName = splitToName.split(",")[0]
            if (string.contains(secondBit)) {
                val splitToType = string.split(secondBit)
                val isolatedType = splitToType[1].split(")")[0]

                classProperties.add(ClassProperties(isolatedName, isolatedType));
            }
        }

        // TODO - look into TornadoFX to see if it only accepts up to 80 char in a string
        //val json = loadJsonObject("""$string""")


    }
}