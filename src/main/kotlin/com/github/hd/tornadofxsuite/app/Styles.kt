package com.github.hd.tornadofxsuite.app

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val main by cssclass()
        val top by cssclass()
        val transparentLayer by cssclass()
    }

    init {

        main {
            backgroundColor += c("#361F27")
        }

        top {
            backgroundColor += c("#DD7549")
            padding = box(20.px)
        }

        transparentLayer {
            backgroundColor += c("#222")
            opacity = 0.0
        }

        button {
            padding = box(10.px)
            alignment = Pos.CENTER
            backgroundColor += c("#DD7549")
            fontWeight = FontWeight.EXTRA_BOLD

            and (hover) {
                backgroundColor += c("#A05434")
                textFill = Color.WHITE
            }
            fontWeight = FontWeight.EXTRA_BOLD
        }

        s(listCell, listCell and even, listCell and odd,
                listCell and selected) {
            backgroundColor += Color.TRANSPARENT
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

        tabPane {
            tabHeaderBackground {
                opacity = 0.0
            }
            tabContentArea {
                borderColor += box(c("#DD7549"))
                borderWidth += box(10.px)
            }
        }

        tab {
            backgroundColor += c("#87462B")
            fontWeight = FontWeight.EXTRA_BOLD
            and (selected) {
                backgroundColor += c("#DD7549")
                textFill = Color.WHITE
                borderColor += box(c("#DD7549"))
            }
        }

    }
}