package apps.amine.bou.readerforselfoss.utils


fun texDrawableFromSource(str: String): String {
    val textDrawable = StringBuilder()
    for (s in str.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
        textDrawable.append(s[0])
    }
    return textDrawable.toString()
}