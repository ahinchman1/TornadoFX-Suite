package com.github.hd.tornadofxsuite.view

import com.github.hd.tornadofxsuite.app.Styles
import com.github.hd.tornadofxsuite.app.Styles.Companion.top
import com.github.hd.tornadofxsuite.controller.FXTestBuilders
import tornadofx.*

class Dialog : Fragment() {

    private val view: MainView by inject()
    private val testBuilder: FXTestBuilders by inject()
    private val scanner: ClassScanner by inject()

    override val root = vbox {
        prefWidth = 600.0
        prefHeight = 400.0

        label("It looks like you have some controls in your project!  Shall I create some tests for them?") {
            prefWidth = 600.0
        }.addClass(top)

        stackpane {
            vboxConstraints {
                marginTopBottom(40.0)
                marginLeftRight(40.0)
            }

            listview<String> {
                scanner.detectedViewControls.forEach { view ->
                    items.add(view.key)
                    view.value.forEach { input ->
                        items.add("\t" + input)
                    }
                }
            }
        }

        button("yeaaa") {
            action {
                testBuilder.generateTests(scanner.detectedViewControls)
<<<<<<< HEAD
                view.overlay.apply {
                    style {
                        opacity = 0.0
                    }
                }
=======
                view.overlay.removeClass(translucent)
>>>>>>> 28f2d6ba961dacc674a7a25c43d65f85f1e38102
                close()
            }
            vboxConstraints {
                marginLeft = 280.0
                marginBottom = 20.0
            }
        }

    }.addClass(Styles.main)

}