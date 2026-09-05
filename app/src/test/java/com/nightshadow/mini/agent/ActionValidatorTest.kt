package com.nightshadow.mini.agent

import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ActionValidatorTest {

    private lateinit var validator: ActionValidator
    private lateinit var mockContext: Context
    private lateinit var mockWindowManager: WindowManager
    private lateinit var mockDisplay: Display

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        mockWindowManager = mock(WindowManager::class.java)
        mockDisplay = mock(Display::class.java)

        `when`(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockWindowManager)
        `when`(mockWindowManager.defaultDisplay).thenReturn(mockDisplay)

        // Mock screen size 1080x1920
        doAnswer { invocation ->
            val metrics = invocation.arguments[0] as DisplayMetrics
            metrics.widthPixels = 1080
            metrics.heightPixels = 1920
            null
        }.`when`(mockDisplay).getRealMetrics(any())

        validator = ActionValidator(mockContext)
    }

    @Test
    fun `valid tap is accepted`() {
        val action = Action(action = "tap", x = 500f, y = 1000f)
        assertTrue(validator.isValid(action))
    }

    @Test
    fun `out of bounds tap is rejected`() {
        val action = Action(action = "tap", x = 2000f, y = 1000f)
        assertFalse(validator.isValid(action))
    }

    @Test
    fun `tap without coordinates is rejected`() {
        val action = Action(action = "tap")
        assertFalse(validator.isValid(action))
    }

    @Test
    fun `valid swipe is accepted`() {
        val action = Action(action = "swipe", direction = "up")
        assertTrue(validator.isValid(action))
    }

    @Test
    fun `invalid swipe direction is rejected`() {
        val action = Action(action = "swipe", direction = "diagonal")
        assertFalse(validator.isValid(action))
    }

    @Test
    fun `unknown action is rejected`() {
        val action = Action(action = "hack_device")
        assertFalse(validator.isValid(action))
    }
}
