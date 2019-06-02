package com.github.hd.tornadofxsuite.controller

import com.github.ast.parser.nodebreakdown.*
import com.github.ast.parser.nodebreakdown.digraph.Digraph
import com.github.hd.tornadofxsuite.app.MapNodesToFunctionsRequest
import tornadofx.*

class NodeMapper: Controller() {
    lateinit var views: MapKClassTo<TestClassInfo>
    lateinit var classBreakDown: ArrayList<ClassBreakDown>

    init {
        subscribe<MapNodesToFunctionsRequest> { event ->
            views = event.viewTestClassInfo
            classBreakDown = event.classBreakDown
            detectViewProperties()
        }
    }

    /**
     * Detect view properties in views, controllers, and otherwise by the view node
     */
    private fun detectViewProperties() {
        classBreakDown.forEach { classInfo ->
            classInfo.properties.forEach { property ->
                if (property.isPropertyViewType()) {
                    mapAssociatedFunctionsToViewElements(classInfo.className, property, classInfo.methods)
                }
            }
        }
    }

    /**
     * For detected view properties, read the methods and map which functions affect certain
     * nodes in a view
     */
    private fun mapAssociatedFunctionsToViewElements(
            className: String,
            property: Property,
            classMethods: ArrayList<Method>
    ) {
        classMethods.forEach { method ->
            method.methodStatements.forEach { statement ->
                // if a method statement contains the view node, it *likely* is a method that affects it.
                // TODO - think about this a bit more
                if (statement.contains(property.name)) {
                    addFunctionAssociation(className, property, method.name)
                }
            }
        }
    }

    /**
     * Locate the [UINode] and add the method to the node.
     *
     * TODO - Should I also add the associated functions to the adjacent nodes? Maybe for just simplicity keep it at the keys
     *
     * TODO - Search for independent functions as well
     */
    private fun addFunctionAssociation(
            className: String,
            property: Property,
            method: String
    ) {
        views[className]?.viewHierachy?.let { digraph ->
            val foundNode = digraph.getNodeByPropertyAssignment(property.name)
            foundNode?.associatedFunctions?.add(method)
        }
    }

    private fun Property.isPropertyViewType(): Boolean {
        return (views.contains(this.type))
    }
}