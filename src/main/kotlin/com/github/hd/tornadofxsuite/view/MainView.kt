package com.github.hd.tornadofxsuite.view

import com.github.hd.tornadofxsuite.app.Styles
import tornadofx.*

class MainView : View("Hello TornadoFX") {
    override val root = hbox {
        label(title) {
            addClass(Styles.heading)
        }
    }
}