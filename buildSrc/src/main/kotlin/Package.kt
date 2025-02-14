object Package {
    const val group = "com.dimension"
    const val name = "Mask"
    const val id = "$group.maskbook"
    val versionName =
        "${Version.main}.${Version.mirror}.${Version.patch}${if (Version.revision.isNotEmpty()) "-${Version.revision}" else ""}"
    const val versionCode = Version.build

    object Version {
        const val main = "2"
        const val mirror = "8"
        const val patch = "0"
        const val revision = "beta02"
        const val build = 69
    }
}
