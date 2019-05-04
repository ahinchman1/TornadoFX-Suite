package com.github.hd.tornadofxsuite.view

import com.github.ast.parser.KParser
import com.github.hd.tornadofxsuite.app.Styles
import com.github.hd.tornadofxsuite.controller.FXTestBuilders
import javafx.scene.control.Button
import tornadofx.*

class Dialog : Fragment() {

    private val view: MainView by inject()
    private val testBuilder: FXTestBuilders by inject()
    // TODO - still on the background thread, needs to be sent back to the main thread
    private val scanner: KParser by inject()

    private lateinit var generateButton: Button

    override val root = vbox {
        prefWidth = 600.0
        prefHeight = 400.0

        tabpane {
            vboxConstraints {
                marginTopBottom(20.0)
                marginLeftRight(40.0)
            }
            tab("UI Hierarchy Analysis") {
                stackpane {
                    vboxConstraints { marginRight = 5.0 }

                    listview<String> {
                        if (scanner.mapClassViewNodes.size > 0) {
                            scanner.mapClassViewNodes.forEach { (className, digraph) ->
                                items.add(className)
                                digraph.viewNodes.forEach { (bucket, children) ->
                                    val nodeLevel = bucket.level
                                    var viewNode = "$nodeLevel \t${bucket.uiNode}"

                                    children.forEachIndexed { index, node ->
                                        viewNode += if (index < children.size) {
                                            " -> ${node.uiNode} "
                                        } else "${node.uiNode}\n"
                                    }
                                    items.add(viewNode)
                                }
                            }
                        }
                    }
                }
                setOnSelectionChanged {
                    generateButton.isVisible = false
                }
            }
            tab("Detected User Controls") {
                stackpane {
                    vboxConstraints { marginLeft = 5.0 }

                    listview<String> {
                        scanner.detectedUIControls.forEach { view ->
                            items.add(view.key)
                            view.value.forEach { input ->
                                items.add("\t${input.uiNode}")
                            }
                        }
                    }
                }
                setOnSelectionChanged {
                    generateButton.isVisible = true
                }
            }
        }

        generateButton = button("Generate Tests") {
            isVisible = false
            action {
                testBuilder.generateTests(view.classesTestInfo)
                view.overlay.apply {
                    style {
                        opacity = 0.0
                    }
                }
                close()
            }
            vboxConstraints {
                marginLeft = 240.0
                marginBottom = 20.0
            }
        }

    }.addClass(Styles.main)

}