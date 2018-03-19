package android.support.v17.leanback.supportleanbackshowcase.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.IOException
import java.io.InputStream

/**
 * Created by susnata on 3/17/18.
 */
class Utils {

    companion object {
        @JvmStatic
        fun convertDpToPixel(ctx: Context, dp: Int): Int {
            val density = ctx.resources.displayMetrics.density
            return Math.round(dp.toFloat() * density)
        }

        /**
         * Will read the content from a given [InputStream] and return it as a [String].
         *
         * @param inputStream The [InputStream] which should be read.
         * @return Returns `null` if the the [InputStream] could not be read. Else
         * returns the content of the [InputStream] as [String].
         */
        @JvmStatic
        fun inputStreamToString(inputStream: InputStream): String? {
            try {
                val bytes = ByteArray(inputStream.available())
                inputStream.read(bytes, 0, bytes.size)
                return String(bytes)
            } catch (e: IOException) {
                return null
            }
        }

        @JvmStatic
        fun getResourceUri(context: Context, resID: Int): Uri {
            return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    context.resources.getResourcePackageName(resID) + '/' +
                    context.resources.getResourceTypeName(resID) + '/' +
                    context.resources.getResourceEntryName(resID))
        }
    }
}
