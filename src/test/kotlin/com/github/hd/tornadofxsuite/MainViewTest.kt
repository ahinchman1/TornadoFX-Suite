package com.github.hd.tornadofxsuite

import javafx.scene.control.ListView
import javafx.scene.control.Button
import com.github.TestView
import com.github.hd.tornadofxsuite.view.MainView
import javafx.scene.Scene
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import javafx.stage.Window
import org.testfx.api.FxAssert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.testfx.api.FxToolkit
import org.testfx.framework.junit.ApplicationTest
import tornadofx.*
import java.io.File
import org.testfx.api.FxRobot
import java.util.*


class TestView: View() {
    override val root = vbox()
}

class MainViewTest: ApplicationTest() {

    val view = TestView()

    lateinit var mainView: MainView
    lateinit var primaryStage: Stage

    lateinit var console: ListView<String>
    lateinit var uploadButton: Button

    @Before
    fun setupFX() {
        primaryStage = FxToolkit.registerPrimaryStage()
        mainView = find()

        view.root.add(mainView.root)

        interact {
            primaryStage.scene = Scene(view.root)
            primaryStage.show()
            primaryStage.toFront()

            console = from(view.root).lookup("#console").query()
            uploadButton = from(view.root).lookup("#uploadProject").query()
        }
    }

    @Test
    fun newTornadoFXSuiteInstance() {
        assertEquals(console.items.size, 1)
        assertEquals(console.items[0].toString(), mainView.consolePath)
    }

    @Test
    fun verifyDirectoryChooserAppears() {
        val home = File(System.getProperty("user.home"))
        clickOn(uploadButton)

        val directoryChooser = getTopModalStage()
        val directoryDialog = directoryChooser?.scene?.root

        if (directoryDialog != null &&
                (DirectoryChooser::class).isInstance(directoryDialog)) {
            assertEquals((directoryDialog as DirectoryChooser).title, "Choose a TornadoFX Project")
        }

        // assertEquals((directoryDialog as DirectoryChooser).title, "Choose a TornadoFX Project")

    }

    private fun getTopModalStage(): Stage? {
        // Get a list of windows but ordered from top[0] to bottom[n] ones.
        // It is needed to get the first found modal window.
        val allWindows = ArrayList<Window>(FxRobot().robotContext().windowFinder.listWindows())
        allWindows.reverse()

        return allWindows
                .filterIsInstance<Stage>()
                .firstOrNull()
    }

}