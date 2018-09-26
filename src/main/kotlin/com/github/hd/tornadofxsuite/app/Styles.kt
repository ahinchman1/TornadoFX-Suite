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
        val translucent by cssclass()
    }

    init {

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

        cell {
            backgroundColor += c("#004600")
            textFill = Color.WHITE
        }

        listView {
            and (focused, selected) {
                backgroundColor += Color.TRANSPARENT
            }
            fitToWidth = true
        }

        translucent {
            fill = c("222")
            opacity = 0.4
        }
    }
}