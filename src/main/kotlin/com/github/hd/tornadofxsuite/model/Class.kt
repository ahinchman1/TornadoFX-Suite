package com.github.hd.tornadofxsuite.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import kotlin.reflect.KClass

data class ClassBreakDown(val className: String,
                          val classAccess: String? = "public",
                          val classConstructors: Fields,
                          val classFields: Fields,
                          val classMethods: Methods)

data class Parameters(val paramName: String,
                      val paramType: String)

// Fields
// var/val nameOfField: Type = initializedValue
// global or local
// if Global & Local then a global field is being used within a class
// if just Global, it is the pointer to the global class
// if just Local, then it is a temporary variable made for the heap
class Fields(fieldName: String, classFieldAccessLevel: String = "public",
             classFieldSignature: String = "var", classFieldInitialized: Boolean,
             fieldGlobal: Boolean, fieldLocal: Boolean) {


    val fieldNameProperty = SimpleStringProperty(this, "", fieldName)
    var fieldName by fieldNameProperty

    // public, private, protected
    val classFieldAccessLevelProperty = SimpleStringProperty(this, "", classFieldAccessLevel)
    var classFieldAccessLevel by classFieldAccessLevelProperty

    // val/var
    val classFieldSignatureProperty = SimpleStringProperty(this, "", classFieldSignature)
    var classFieldSignature by classFieldSignatureProperty


    // initialized v uninitialized, detects "lateinit" or not
    val classFieldInitializedProperty = SimpleBooleanProperty(this, "", classFieldInitialized)
    var classFieldInitialized by classFieldInitializedProperty

    val fieldGlobal = SimpleBooleanProperty(this, "")

}

// fun method(params: ParamType): returnsType { other calls }
class Methods(methodName: String, methodAccess: String = "public",
                   methodParams: Fields, returnsType: String = "void", mappedMethods: Array<String>) {

    val methodNameProperty = SimpleStringProperty(this, "", methodName)
    var methodName by methodNameProperty

    val methodAccessProperty = SimpleStringProperty(this, "", methodAccess)
    var methodAccess by methodAccessProperty

    val methodParamsProperty = SimpleObjectProperty(this, "", methodParams)
    var methodParams by methodParamsProperty

    val returnsTypeProperty = SimpleStringProperty(this, "", returnsType)
    var returnsType by returnsTypeProperty

}

class classScope: Scope() {

}