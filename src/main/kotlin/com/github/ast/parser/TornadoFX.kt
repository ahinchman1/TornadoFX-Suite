package com.github.ast.parser

// TODO - Add Model support
enum class MODELS {
    ItemViewModel, ViewModel
}

// TODO
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

data class TornadoFXView(var view: String? = "", var type: String? = "", var scope: String? = "")