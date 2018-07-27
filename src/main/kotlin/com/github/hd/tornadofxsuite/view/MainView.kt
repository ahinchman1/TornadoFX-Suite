package com.github.hd.tornadofxsuite.view

import com.github.hd.tornadofxsuite.app.Styles
import javafx.application.Platform
import tornadofx.*
import kotlin.concurrent.thread

class MainView : View("TornadoFX-Suite") {
    override val root = vbox {
        prefWidth = 400.0
        prefHeight = 600.0

        hbox {
            imageview("tornado-fx-logo.png")
            hboxConstraints {
                marginLeftRight(20.0)
                marginTopBottom(20.0)
            }
        }.addClass(Styles.top)

        progressindicator {
            thread {
                for (i in 1..100) {
                    Platform.runLater { progress = i.toDouble() / 100.0 }
                    Thread.sleep(100)
                }
            }
        }

    }.addClass(Styles.mainScreen)
}