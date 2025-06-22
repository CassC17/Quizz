import android.util.Base64

fun String.decodeBase64(): String {
    return String(Base64.decode(this, Base64.DEFAULT), charset("UTF-8"))
}
