import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {

    @Test
    fun `a final release ends in 99`() {
        assertEquals(10_200_399, AppVersion.code("1.2.3"))
        assertEquals(10_000_099, AppVersion.code("1.0.0"))
    }

    @Test
    fun `a release candidate ends in its number`() {
        assertEquals(10_200_301, AppVersion.code("1.2.3-rc1"))
        assertEquals(10_200_398, AppVersion.code("1.2.3-rc98"))
    }

    @Test
    fun `a release candidate sorts below its final release and above the previous one`() {
        assertTrue(AppVersion.code("1.2.3-rc9") < AppVersion.code("1.2.3"))
        assertTrue(AppVersion.code("1.2.2") < AppVersion.code("1.2.3-rc1"))
    }

    @Test
    fun `versions sort by major, then minor, then patch`() {
        assertTrue(AppVersion.code("1.99.999") < AppVersion.code("2.0.0"))
        assertTrue(AppVersion.code("1.2.999") < AppVersion.code("1.3.0"))
    }

    @Test
    fun `the largest supported version fits in an Int`() {
        assertEquals(2_149_999_999, 214L * 10_000_000 + 99 * 100_000 + 999 * 100 + 99)
        assertEquals(2_149_999_999.toInt(), AppVersion.code("214.99.999"))
    }

    @Test
    fun `the dev name gives 1`() {
        assertEquals(1, AppVersion.code(AppVersion.DEV_NAME))
    }

    @Test
    fun `other suffixes are rejected`() {
        listOf("1.2.3-beta", "1.2.3-rc", "1.2.3-rc0", "1.2.3-rc99", "1.2.3+build5", "1.2", "v1.2.3", "").forEach {
            assertThrows(it, IllegalArgumentException::class.java) { AppVersion.code(it) }
        }
    }

    @Test
    fun `parts that would overflow are rejected`() {
        listOf("215.0.0", "1.100.0", "1.0.1000").forEach {
            assertThrows(it, IllegalArgumentException::class.java) { AppVersion.code(it) }
        }
    }

    @Test
    fun `0_0_0 is a normal version, only the dev name is special`() {
        assertEquals(99, AppVersion.code("0.0.0"))
    }
}
