package com.github.hd.tornadofxsuite.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.util.*

class BareBreakDown(className: String,
                    classProperties: ArrayList<ClassProperties>,
                    classMethods: ArrayList<String>) {
    val classNameProperty = SimpleStringProperty(this, "", className)
    var className by classNameProperty

    val classPropertiesProperty = SimpleObjectProperty(this, "", classProperties)
    var classProperties by classPropertiesProperty

    val classMethodsProperties = SimpleObjectProperty(this, "", classMethods)
    var classMethods by classMethodsProperties
}

class BareBreakDownModel : ItemViewModel<BareBreakDown>() {
    val className = bind(BareBreakDown::classNameProperty)
    val classProperties = bind(BareBreakDown::classPropertiesProperty)
    val classMethods = bind(BareBreakDown::classMethodsProperties)
}


class ClassProperties(propertyName: String,
                      propertyType: String) {
    val propertyNameProperty = SimpleStringProperty(this, "", propertyName)
    var propertyName by propertyNameProperty

    val propertyTypeProperty = SimpleStringProperty(this, "", propertyType)
    var propertyType by propertyTypeProperty
}

class ClassPropertiesModel : ItemViewModel<ClassProperties>() {
    val propertyName = bind(ClassProperties::propertyNameProperty)
    val propertyType = bind(ClassProperties::propertyTypeProperty)
}

// TODO implement an infinite tree structure to hold View structures

enum class MVC {
    ItemViewModel, ViewModel, Controller, View
}

enum class COMPONENTS {
    BORDERPANE, LISTVIEW, TABLEVIEW, VBOX, HBOX,
    DATAGRID, IMAGEVIEW
}

enum class INPUTS {
    Form, TextField, DateField, Button, Action,
    RadioButton, ToggleButton, ComboButton, Checkbox,
    Item, Paginator, PasswordField
}

class TornadoFXInputs(viewClass: String, inputs: ArrayList<String>) {
    val viewClassProperty = SimpleStringProperty(this, "", viewClass)
    var viewClass by viewClassProperty

    val inputsProperty = SimpleObjectProperty(this, "", inputs)
    var inputs by inputsProperty
}

class TornadoFXInputsModel: ItemViewModel<TornadoFXInputs>() {
    val ownerName = bind(TornadoFXInputs::viewClassProperty)
    val catName = bind(TornadoFXInputs::inputsProperty)
}

class TornadoFXInputsScope:  Scope() {
    var collection = HashMap<String, ArrayList<String>>()
}





