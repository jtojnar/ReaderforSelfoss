package apps.amine.bou.readerforselfoss.utils


fun String.toTextDrawableString(): String {
    val textDrawable = StringBuilder()
    for (s in this.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
        textDrawable.append(s[0])
    }
    return textDrawable.toString()
}