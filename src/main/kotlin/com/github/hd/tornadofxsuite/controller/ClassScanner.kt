package com.example.demo.controller

import com.github.hd.tornadofxsuite.model.BareBreakDown
import com.github.hd.tornadofxsuite.model.ClassProperties
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kastree.ast.Node
import kastree.ast.psi.Parser
import tornadofx.*



/*
 * TODO - AST mappings, composition.
 */
class ClassScanner: Controller() {

    var bareClasses = ArrayList<BareBreakDown>()
    var independentFunctions = ArrayList<String>()
    var detectedViewControls = ArrayList<String>()

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
        val classMembers = ArrayList<String>()

        // Save for all files
        (file.decls[0] as Node.Decl.Structured).members.forEach {
            when (it) {
                is Node.Decl.Structured -> println("this is probably a companion object")
                is Node.Decl.Property -> convertToJsonProperty(it, classProperties, file)
                is Node.Decl.Func -> classMembers.add(it.name)
            }
        }
        bareClasses.add(BareBreakDown(className, classProperties, classMembers))
    }

    // TODO - refactor with the same strategy used in detectLambdaControls
    private fun convertToJsonProperty(property: Node.Decl.Property, propList: ArrayList<ClassProperties>, file: Node.File) {
        val firstTrueBit = "Property(mods=[], readOnly=true, typeParams=[], " +
                "receiverType=null, vars=[Var(name="
        val firstFalseBit = "Property(mods=[], readOnly=false, typeParams=[], " +
                "receiverType=null, vars=[Var(name="
        val privateFirstTrueBit = "Property(mods=[Lit(keyword=PRIVATE)], " +
                "readOnly=true, typeParams=[], receiverType=null, vars=[Var(name="
        val privateFirstFalseBit = "Property(mods=[Lit(keyword=PRIVATE)], " +
                "readOnly=false, typeParams=[], receiverType=null, vars=[Var(name="
        val root = "Property(mods=[Lit(keyword=OVERRIDE)], readOnly=true, typeParams=[], receiverType=null, vars=[Var(name=root"
        val secondBit = "expr=Call(expr=Name(name="
        val string = property.toString()

        val gson = Gson()
        val nodesElement: JsonElement = gson.toJsonTree(property)

        // it ain't stupid if it works shut up
        if (string.contains(firstTrueBit) || string.contains(secondBit) ||
                string.contains(privateFirstTrueBit) || string.contains(privateFirstFalseBit)) {
            val splitToName = when {
                string.contains(firstTrueBit) -> string.split(firstTrueBit)[1]
                string.contains(privateFirstTrueBit) -> string.split(privateFirstTrueBit)[1]
                string.contains(firstFalseBit) -> string.split(firstFalseBit)[1]
                string.contains(privateFirstFalseBit) -> string.split(privateFirstFalseBit)[1]
                else -> {
                    // Detect view controls, return root
                    detectLambdaControls(nodesElement.asJsonObject)
                    println("DETECTED LAMBDA ELEMENTS: ")
                    detectedViewControls.forEach {
                        println(it)
                    }
                    "root"
                }
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

    // recursively loop in the order of lambda.func.block.stmts
    private fun detectLambdaControls(node: JsonObject) {
        val root = node.get("expr")

        if (root.asJsonObject.get("lambda") != null) {
            val rootName = root.asJsonObject
                    .get("expr").asJsonObject
                    .get("name")

            detectedViewControls.add(rootName.asString)

            // get elements in lambda
            val lambda = root.asJsonObject.get("lambda").asJsonObject
            val elements: JsonArray = lambda.get("func").asJsonObject
                    .get("block").asJsonObject
                    .get("stmts") as JsonArray

            elements.forEach {
                detectLambdaControls(it.asJsonObject)
            }
        }
    }

    // using the enum class to check for control values here
    private inline fun <reified T : Enum<T>> addControls(control: String) {
        enumValues<T>().forEach {
            if (control.toLowerCase() == (it.name).toLowerCase()) {
                detectedViewControls.add(control)
            }
        }
    }
}