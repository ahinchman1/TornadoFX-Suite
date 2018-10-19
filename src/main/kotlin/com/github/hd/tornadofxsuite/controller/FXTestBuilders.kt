package com.github.hd.tornadofxsuite.controller

import tornadofx.*
import java.io.File
import java.util.ArrayList
import java.util.HashMap

class FXTestBuilders : Controller() {

    // Dictionary of testing components
    fun generateTests(collection: HashMap<String, ArrayList<String>>) {
        var file = "import javafx.stage.Stage\n" +
                "import org.junit.Test\n" +
                "import org.testfx.api.FxToolkit\n" +
                "import org.testfx.framework.junit.ApplicationTest\n" +
                "import tornadofx.*\n" +
                "import kotlin.test.assertEquals\n\n" +
                "class UITest {\n" +
                setup()

        collection.forEach {
            val classname = it.key
            it.value.forEach {
                file += testStub(classname, it)
            }
        }

        // write string to document
        File("UITest.kt").printWriter().use {out -> out.println(file)}
    }

    private fun setup(): String {
        return "\t@Before\n" +
                "\tfun setup() {\n" +
                "\t\tFxToolkit.registerPrimaryStage()\n" +
                "\t}\n\n"
    }

    private fun replicateView(): String {
        return ""
    }

    // start here with PBT
    private fun testStub(className: String, node: String): String {
        return "\t@Test\n" +
                "\tfun test" + node + "() { \n" +
                "\t\t\n" +
                "}\n\n"
    }

    private fun buildTextFieldTest(): String {
        return ""
    }

    private fun buildButtonTest(): String {
        return ""
    }

    private fun buildToggleButtonTest(): String {
        return ""
    }

}