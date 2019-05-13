package com.github
/**
import java.util.logging.Level.SEVERE
import java.io.IOException
import com.sun.xml.internal.ws.streaming.XMLStreamReaderUtil.close
import org.junit.AfterClass
import javafx.scene.layout.AnchorPane
import org.testfx.matcher.base.WindowMatchers.isShowing
import javafx.scene.layout.HBox
import com.sun.javaws.Main.launchApp
import javafx.application.Platform
import javafx.scene.control.TextField
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.testfx.framework.junit.ApplicationTest


class CompleteFieldTesting : ApplicationTest() {


    /**
     * This test makes sure that if we set all of the values to 0, the test fails with
     * an 'invalid input' option.
     */
    @Test
    fun verifyThatAllBlankFieldsGivesErrorPopup() {
        assertNotNull(Mark.stage_returner())
        (GuiTest.find("#tvd_msl_id") as TextField).setText("0")
        (GuiTest.find("#depth_input") as TextField).setText("0")
        (GuiTest.find("#cf_f_if_north") as TextField).setText("0")
        (GuiTest.find("#cf_f_if_east") as TextField).setText("0")
        controller!!.click("#cf_f_b_calculate_id")
        assertTrue(GuiTest.find("#error_label_top").isVisible())

    }

    /**
     * This is probably one of the more complicated tests, it takes a set of pre-checked
     * data and uses the calculator to check the results, it then verifies each of the
     * calculated results against a pre-checked data set. if any of the numbers are off at
     * all (to any amount of significant digits), the test will fail.
     *
     */
    @Test
    fun verifyValidDataProducesValidOutput() {
        assertNotNull(Main.stage_returner())

        (GuiTest.find("#tvd_msl_id") as TextField).setText("Number Removed For privacy")
        (GuiTest.find("#depth_input") as TextField).setText("Number Removed For privacy")
        (GuiTest.find("#cf_f_if_north") as TextField).setText("Number Removed For privacy")
        (GuiTest.find("#cf_f_if_east") as TextField).setText("Number Removed For privacy")
        val dateField = (GuiTest.find("#hBox2") as HBox).children[0] as TextField
        dateField.setText("")
        dateField.setText(dateField.getText().trim() + "01/Sep/2013")
        while ((GuiTest.find("#cf_f_b_calculate_id") as Button).getText().equals("Calculate")) {
            controller!!.click("#cf_f_b_calculate_id")
            if (Main.stage_returner().getScene().lookup("#cf_f_b_calculate_id").isVisible()) {
            } else
                break
        }
        assertTrue((GuiTest.find("#cf_f_tf_actualDepth_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_dec_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_dip_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_tmi_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_xtmi_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_xdec_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_xdip_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_zdec_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_zdip_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_ztmi_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_ydec_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_ydip_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_ytmi_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_otdec_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_otdip_id") as TextField).getText().equals("Number Removed For privacy"))
        assertTrue((GuiTest.find("#cf_f_df_ottmi_id") as TextField).getText().equals("Number Removed For privacy"))
    }

    /**
     * This is a quick test, it simply clicks all of the main menu items (not the sub-menus) to make sure they are, indeed,
     * clickable.
     *
     */
    @Test
    fun verifyAllMenuItemsAreClickable() {

        controller!!.click("Tools").click("File").click("Windows").click("Data").click("About").click("Help")
    }


    /**
     * This test makes sure that the report window shows up after some valid data is entered.
     */
    @Test
    fun verifyThatReportStageAppearsAfterValidDataIsCalculated() {
        assertNotNull(Main.stage_returner())

        (GuiTest.find("#tvd_msl_id") as TextField).setText("1000")
        (GuiTest.find("#depth_input") as TextField).setText("0")
        (GuiTest.find("#cf_f_if_north") as TextField).setText("1495005")
        (GuiTest.find("#cf_f_if_east") as TextField).setText("3371001")
        val dateField = (GuiTest.find("#hBox2") as HBox).children[0] as TextField
        dateField.setText("")
        dateField.setText(dateField.getText().trim() + "01/Sep/2013")
        if (Main.stage_returner().getScene().lookup("#cf_f_b_calculate_id").isVisible()) {
            while ((GuiTest.find("#cf_f_b_calculate_id") as Button).getText().equals("Calculate")) {
                controller!!.click("#cf_f_b_calculate_id")
                if (Main.stage_returner().getScene().lookup("#cf_f_b_calculate_id").isVisible()) {
                } else
                    break
            }
        }
        controller!!.click("Report")
        assertTrue(ReportFileChooserWindow.get_stage().isShowing())
        Platform.runLater(Runnable { ReportFileChooserWindow.get_stage().close() })
        //This might seem like a hack, and well, it sort of is. But it forces the
        //'calculate' button to come back, since some tests rely on it being there initially.  yep.
        (GuiTest.find("#tvd_msl_id") as TextField).setText("abcd")
    }

    /**
     * This test makes sure that the version window popup actually appears when you press the
     * 'version' option of the menus underneath 'help'
     */
    @Test
    fun verifyThatVersionWindowAppearsProperly() {
        controller!!.click("#menuBar")
        controller!!.click("Help").click("Calculator Version")
        assertTrue(VersionWindow.popup_returner().isShowing())
        controller!!.click("File")
    }

    /**
     * This test tries to open the fileChooser window, that's it.
     */
    @Test
    fun verifyThatTheFileChooserWindowAppearsProperly() {
        controller!!.click("#menuBar")
        controller!!.click("Tools")
        controller!!.click("Read File")
        assertTrue(FileChooserWindow.get_stage().isShowing())
        Platform.runLater(Runnable { FileChooserWindow.get_stage().close() })
    }


    /**
     * This test is to make sure that the metadata window appears as it should for this particular cube.
     * This does NOT test the contents of the window, simply the existence.
     */
    @Test
    fun verifyThatMetaDataWindowAppearsProperly() {
        controller!!.click("#menuBar")
        controller!!.click("Data")
        controller!!.click("Metadata")
        assertTrue(MetadataWindow.metadataInstanceReturner().isShowing())
        Platform.runLater(Runnable { MetadataWindow.metadataInstanceReturner().close() })
    }

    /**
     * This test makes sure that the GUI can switch to and from HDGM without trouble.
     */
    @Test
    fun verifySwitchToandFromHDGMWorks() {
        controller!!.click("#menuBar")
        controller!!.click("Windows")
        controller!!.click("Residual against HDGM")
        assertTrue((GuiTest.find("#main_anchor") as AnchorPane).prefHeightProperty().value == 341.0)
        //Here you can see the use of 'changing the state' mid-way through, more-or-less, this gives us access to a whole slew of new controls on the new window that I can then access.
        controller = object : GuiTest() {
            protected val rootNode: Parent
                get() = Mark.stage_returner().getScene().getRoot()
        }
        controller!!.click("#menuBar")
        controller!!.click("File").click("Windows")
        controller!!.click("Complete Field")

    }

    /**
     * This tests makes sure that the GUI can switch to and from BGGM without trouble.
     */
    @Test
    fun verifySwitchToandFromBGGMWorks() {
        controller!!.click("#menuBar")
        controller!!.click("Windows")
        controller!!.click("Residual against BGGM")
        assertTrue((GuiTest.find("#main_anchor") as AnchorPane).prefHeightProperty().value == 344.0)
        controller = object : GuiTest() {
            protected val rootNode: Parent
                get() = Mark.stage_returner().getScene().getRoot()
        }
        controller!!.click("#menuBar")
        controller!!.click("File").click("Windows")
        controller!!.click("Complete Field")

    }

    companion object {

        private var controller: GuiTest? = null


        /**
         * This sets up the initial class and gets us to the stage we want to be at for the rest of this set of tests.
         * this should be the shortest, and most robust way to get the certain stage, since we don't want any of these
         * tests to break for a reason other than them being on their respective stage.
         */
        @BeforeClass
        fun setUpClass() {
            FXTestUtils.launchApp(Main::class.java)

            //here is that closure I talked about above, you instantiate the getRootNode abstract method
            //which requires you to return a 'parent' object, luckily for us, getRoot() gives a parent!
            //getRoot() is available from ALL Node objects, which makes it easy.
            controller = object : GuiTest() {
                protected val rootNode: Parent
                    get() = Main.stage_returner().getScene().getRoot()
            }
            println(System.getProperty("user.dir"))
            (GuiTest.find("#ncw_f_tf_File") as TextField).setText(System.getProperty("user.dir") + "Whatever_text_you_want")
            controller!!.click("#main_button")


        }

        /**
         * This makes sure that all of the processes shut down when they are supposed to, especially since these test
         * open and close the GUI so many times, without this, the program goes crazy.
         */
        @AfterClass
        fun shutdownAll() {
            Platform.runLater(Runnable { Main.stage_returner().close() })
            try {
                Runtime.getRuntime().exec("taskkill /F /IM external_program.exe")
            } catch (ex: IOException) {
                Log4jLogger.rootLogger().error(Level.SEVERE, ex)
            }

        }
    }

}*/