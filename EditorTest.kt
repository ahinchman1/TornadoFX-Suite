import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField
import org.junit.Test
import tornadofx.*
import kotlin.reflect.KClass
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import junit.framework.Assert.*
import org.junit.Before
import org.testfx.api.FxToolkit
import org.testfx.framework.junit.ApplicationTest
import com.example.demo.view.Editor.kt

data class Control(
    val type: KClass<out Node>,
    val id: String,
    val isAssigned: Boolean = false
)

class TestView: View() {
    override val root = vbox()
}

class EditorTest: ApplicationTest {

    lateinit var primaryStage: Stage

    val view = TestView()

	private val listOfControls = listOf(
		Control(Form::class, "AfjLgzw")
		Control(TextField::class, "enIYmAX")
		Control(TextField::class, "dZvba4Z")
		Control(TextField::class, "eaie3yO")
		Control(Button::class, "1jMowpb")

	)

	lateinit var AfjLgzwForm: Form
	lateinit var enIYmAXTextField: TextField
	lateinit var dZvba4ZTextField: TextField
	lateinit var eaie3yOTextField: TextField
	lateinit var 1jMowpbButton: Button

    @Before
    fun setup() {
        primaryStage = FxToolkit.registerPrimaryStage()

        val editorFragment = find<Editor>()

        view.root.add(editorFragment.root)
        addAllIdsToDescendants(view.root)

        interact {
            primaryStage.scene = Scene(view.root)
            primaryStage.show()
            primaryStage.toFront()
        
			enIYmAXTextField = from(view.root).lookup(#enIYmAX).query()
			dZvba4ZTextField = from(view.root).lookup(#dZvba4Z).query()
			eaie3yOTextField = from(view.root).lookup(#eaie3yO).query()
			1jMowpbButton = from(view.root).lookup(#1jMowpb).query()		}
	}
	private fun addAllIdsToDescendants(parent: Parent) {
	    for (node in parent.childrenUnmodifiable) {
	        checkToAttachNodeId(node)
	        if (node is Parent) {
	            addAllIdsToDescendants(node)
	        }
	    }
	}
	
	private fun checkToAttachNodeId(node: Node) {
	    listOfControls.forEach { control ->
	        if (control.type.isInstance(node) &&
	                !control.isAssigned &&
	                node.id == null) {
	            node.id = control.id
	            control.isAssigned
	        }
	    }
	}

	@Test
	fun testenIYmAXTextField() {
		assertNotNull(enIYmAXtextfield.text)
		assertTrue(!enIYmAXtextfield.text.isNullOrEmpty())
		
		clickOn(enIYmAXTextField).write("Something")
		assertEquals("Something", enIYmAXTextField.text)
	}
	@Test
	fun testdZvba4ZTextField() {
		assertNotNull(dZvba4Ztextfield.text)
		assertTrue(!dZvba4Ztextfield.text.isNullOrEmpty())
		
		clickOn(dZvba4ZTextField).write("Something")
		assertEquals("Something", dZvba4ZTextField.text)
	}
	@Test
	fun testeaie3yOTextField() {
		assertNotNull(eaie3yOtextfield.text)
		assertTrue(!eaie3yOtextfield.text.isNullOrEmpty())
		
		clickOn(eaie3yOTextField).write("Something")
		assertEquals("Something", eaie3yOTextField.text)
	}
	@Test
	fun test1jMowpbButton() {
		clickOn(1jMowpbButton)
	}
}
