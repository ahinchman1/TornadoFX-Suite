package com.github.hd.tornadofxsuite.app

import com.github.hd.tornadofxsuite.view.MainView
import tornadofx.*

class TornadoFXSuite : App(MainView::class, Styles::class) {
    init {
        reloadViewsOnFocus()
        reloadStylesheetsOnFocus()
    }
}