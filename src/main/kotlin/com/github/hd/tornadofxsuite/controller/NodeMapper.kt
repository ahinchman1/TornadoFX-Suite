package com.github.hd.tornadofxsuite.controller

import com.github.ast.parser.nodebreakdown.*
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
     * Detect view properties in views, controllers, and otherwise.
     */
    fun detectViewProperties() {
        classBreakDown.forEach { classInfo ->
                classInfo.classProperties.forEach { property ->
                    if (property.isPropertyViewType()) {
                        mapFunctionstoViewElements(property, classInfo.classMethods)
                    }
                }
            }
    }

    /**
     * For detected view properties, read the methods and map which functions affect certain
     * nodes in a view
     */
    fun mapFunctionstoViewElements(property: Property, classMethods: ArrayList<Method>) {

    }

    private fun Property.isPropertyViewType(): Boolean {
        return (views.contains(this.propertyType))
    }
}