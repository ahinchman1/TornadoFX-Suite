package com.github.ast.parser

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.util.ArrayList
import java.util.HashMap

enum class MODELS {
    ItemViewModel, ViewModel
}

enum class COMPONENTS {
    View, Fragment
}

enum class NODES {
    BorderPane, ListView, TableView, VBox, HBox,
    DataGrid, ImageView, GridPane, Row, Form, FieldSet,
    TextField, Button, DateField, ComboButton, ComboForm,
    CheckBox, Paginator, PasswordField, TreeView, TabView
}

// detected inputs to test, will build over time
enum class INPUTS {
    TextField, Button, Form
}

class TornadoFXInputs(viewClass: String, inputs: ArrayList<String>) {
    val viewClassProperty = SimpleStringProperty(this, "", viewClass)
    var viewClass by viewClassProperty

    val inputsProperty = SimpleObjectProperty(this, "", inputs)
    var inputs by inputsProperty
}