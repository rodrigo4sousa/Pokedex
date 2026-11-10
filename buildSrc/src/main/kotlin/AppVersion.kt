/**
 * Turns a version name into an Android versionCode.
 *
 * The name is `MAJOR.MINOR.PATCH`, optionally followed by `-rcN`:
 *
 *     code = major * 10_000_000 + minor * 100_000 + patch * 100 + (N for -rcN, 99 for a final release)
 *
 * so 1.2.3-rc1 is 10200301 and 1.2.3 is 10200399, and a release candidate always sorts below its final
 * release. Any other suffix is rejected, and so are values that would overflow a part of the code
 * (major above 214, minor above 99, patch above 999, rc number outside 1..98).
 */
object AppVersion {

    /** The name used when there is no tag and no -PappVersionName. */
    const val DEV_NAME = "0.0.0-dev"
    private const val DEV_CODE = 1

    private val pattern = Regex("""^(\d+)\.(\d+)\.(\d+)(?:-rc(\d+))?$""")

    fun code(name: String): Int {
        if (name == DEV_NAME) return DEV_CODE
        val match = requireNotNull(pattern.matchEntire(name)) {
            "Unsupported version '$name': use MAJOR.MINOR.PATCH or MAJOR.MINOR.PATCH-rcN"
        }
        val (major, minor, patch) = match.groupValues.subList(1, 4).map { it.toLong() }
        val rc = match.groupValues[4].takeIf { it.isNotEmpty() }?.toLong()
        require(major in 0..214) { "Major version out of range (0..214) in '$name'" }
        require(minor in 0..99) { "Minor version out of range (0..99) in '$name'" }
        require(patch in 0..999) { "Patch version out of range (0..999) in '$name'" }
        require(rc == null || rc in 1..98) { "The rc number must be 1..98 in '$name'" }
        val code = major * 10_000_000 + minor * 100_000 + patch * 100 + (rc ?: 99)
        require(code >= 1) { "Version '$name' gives a versionCode below 1" }
        return code.toInt()
    }
}
