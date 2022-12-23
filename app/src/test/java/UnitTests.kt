package `in`.samlav.eartrainer.test

import `in`.samlav.eartrainer.TestFragment
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioTests {
    @Test fun calculateP() {
        assertEquals(null, 16422f, TestFragment().calculateP(3f), 1f)
    }
}