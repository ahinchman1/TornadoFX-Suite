package com.github.hd.tornadofxsuite.view

import com.example.demo.controller.ClassScanner
import com.github.hd.tornadofxsuite.app.Styles.Companion.translucent
import javafx.scene.control.ListView
import javafx.scene.paint.Color
import tornadofx.*

class Dialog : Fragment() {

    private val scanner: ClassScanner by inject()
    private val view: MainView by inject()

    override val root = vbox {
        prefWidth = 400.0
        prefHeight = 200.0

        label("It looks like you have some controls in your project.")
        listview<String> {
            items.add("test")
            // TODO - find a way to display these
            scanner.detectedViewControls.forEach {
                items.add(it.toString())
            }
            style {
                backgroundColor += Color.CORNFLOWERBLUE
            }
        }
        button("Ok") {
            action {
                view.overlay.removeClass(translucent)
                close()
            }
        }
    }

}