package com.github.hd.tornadofxsuite.app

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val mainScreen by cssclass()
        val top by cssclass()
    }

    init {
        label and heading {
            padding = box(10.px)
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
        }

        mainScreen {
            backgroundColor += c("#222")
        }

        top {
            backgroundColor += Color.PERU
            padding = box(20.px)
        }
    }
}