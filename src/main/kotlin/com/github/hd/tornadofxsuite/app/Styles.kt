package com.github.hd.tornadofxsuite.app

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val mainScreen by cssclass()
        val top by cssclass()
        val console by cssclass()
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

        button {
            backgroundColor += Color.PERU
            padding = box(10.px)
            alignment = Pos.CENTER

            and (hover) {
                backgroundColor += c("#ad6625")
            }
        }

        console {
            backgroundColor += c("#004600")
        }
    }
}