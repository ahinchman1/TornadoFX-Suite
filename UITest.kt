import javafx.stage.Stage
import org.junit.Test
import org.testfx.api.FxToolkit
import org.testfx.framework.junit.ApplicationTest
import tornadofx.*
import kotlin.test.assertEquals

class UITest {
	@Before
	fun setup() {
		FxToolkit.registerPrimaryStage()
	}


