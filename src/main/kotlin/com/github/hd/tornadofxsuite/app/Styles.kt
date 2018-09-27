package com.github.hd.tornadofxsuite.app

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val main by cssclass()
        val top by cssclass()
        val translucent by cssclass()
        val dialog by cssclass()
    }

    init {

        main {
            backgroundColor += c("#361F27")
        }

        top {
            backgroundColor += c("#DD7549")
            padding = box(20.px)
        }

        button {
            backgroundColor += c("#DD7549")
            padding = box(10.px)
            alignment = Pos.CENTER

            and (hover) {
                backgroundColor += c("#B25E3A")
            }
            fontWeight = FontWeight.EXTRA_BOLD
        }

        cell {
            backgroundColor += c("#39393A")
            textFill = Color.WHITE
        }

        listView {
            and (selected) {
                backgroundColor += Color.TRANSPARENT
            }
            backgroundColor += c("#39393A")
            fitToWidth = true
        }

        s(listCell, listCell and even, listCell and odd,
                listCell and selected) {
            backgroundColor += Color.TRANSPARENT
        }
    }
}