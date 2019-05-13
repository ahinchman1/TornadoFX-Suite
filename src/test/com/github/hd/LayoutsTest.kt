package com.github.hd

import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.testfx.api.FxToolkit
import org.testfx.framework.junit.ApplicationTest
import tornadofx.*

class ViewTest : ApplicationTest() {
    val primaryStage: Stage = FxToolkit.registerPrimaryStage()

    lateinit var pane: StackPane

    @Test
    fun testFitToParentSize() {
        FxToolkit.setupFixture {
            val root = Pane().apply {
                stackpane {
                    this@ViewTest.pane = this
                    fitToParentSize()
                }
                setPrefSize(400.0, 160.0)
            }
            primaryStage.scene = Scene(root)
            primaryStage.show()

            assertEquals(root.width, pane.width)
            assertEquals(root.height, pane.height)
        }
    }
}