package com.github.ast.parser

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.util.ArrayList
import java.util.HashMap

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