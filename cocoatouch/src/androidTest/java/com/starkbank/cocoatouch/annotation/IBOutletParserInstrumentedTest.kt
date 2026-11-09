package com.starkbank.cocoatouch.annotation

import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.onMain
import com.starkbank.cocoatouch.uikit.UIApplication
import com.starkbank.cocoatouch.uikit.UILabel
import com.starkbank.cocoatouch.uikit.UISwitch
import com.starkbank.cocoatouch.uikit.UIView
import com.starkbank.cocoatouch.uikit.UIViewController
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

open class PrivateShadowBaseFixture : UIViewController() {

    @IBOutlet(1) private lateinit var label: UILabel

    fun baseLabel(): UILabel = label
}

class PrivateShadowChildFixture : PrivateShadowBaseFixture() {

    @IBOutlet(3) lateinit var label: UILabel
}

open class OverriddenBaseFixture : UIViewController() {

    @IBOutlet(1) open var label: UILabel? = null
}

class OverriddenChildFixture : OverriddenBaseFixture() {

    @IBOutlet(2) override var label: UILabel? = null
}

open class IsPrefixedBaseFixture : UIViewController() {

    @IBOutlet(1) open var isEnabledSwitch: UISwitch? = null
}

class IsPrefixedChildFixture : IsPrefixedBaseFixture() {

    @IBOutlet(2) override var isEnabledSwitch: UISwitch? = null
}

class UnshadowedFixture : UIViewController() {

    @IBOutlet(1) lateinit var label: UILabel
}

@RunWith(AndroidJUnit4::class)
class IBOutletParserInstrumentedTest {

    private lateinit var scenario: ActivityScenario<TestStoryboard>

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
    }

    @After
    fun close() {
        scenario.close()
    }

    @Test
    fun aPrivateParentFieldAndASubclassFieldWithTheSameNameAreBothBound() {
        onMain {
            val view1 = View(UIApplication.context()).apply { id = 1 }
            val view3 = View(UIApplication.context()).apply { id = 3 }
            val child = PrivateShadowChildFixture()
            child.view = viewHosting(view1, view3)
            IBOutletParser.parse(child)
            assertSame(view1, child.baseLabel().widget)
            assertSame(view3, child.label.widget)
        }
    }

    @Test
    fun anOverriddenOutletBindsOnlyTheSubclassWhenTheParentIdIsAbsent() {
        onMain {
            val view2 = View(UIApplication.context()).apply { id = 2 }
            val child = OverriddenChildFixture()
            child.view = viewHosting(view2)
            IBOutletParser.parse(child)
            assertSame(view2, child.label!!.widget)
        }
    }

    @Test
    fun anIsPrefixedOverriddenOutletBindsOnlyTheSubclassWhenTheParentIdIsAbsent() {
        onMain {
            val view2 = View(UIApplication.context()).apply { id = 2 }
            val child = IsPrefixedChildFixture()
            child.view = viewHosting(view2)
            IBOutletParser.parse(child)
            assertSame(view2, child.isEnabledSwitch!!.widget)
        }
    }

    @Test
    fun anUnshadowedOutletWhoseIdIsAbsentThrows() {
        onMain {
            val fixture = UnshadowedFixture()
            fixture.view = viewHosting()
            val error = assertThrows(IllegalStateException::class.java) { IBOutletParser.parse(fixture) }
            assertTrue(error.message!!.contains("not found"))
        }
    }

    private fun viewHosting(vararg children: View): UIView {
        val root = FrameLayout(UIApplication.context())
        for (child in children) {
            root.addView(child)
        }
        return UIView().apply { widget = root }
    }
}
