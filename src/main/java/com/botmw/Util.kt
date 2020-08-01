package com.botmw

import org.json.JSONArray
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.util.*

/**
 *
 * @author Bot
 */
object Util {
    fun getURLParameters(url: String): Map<String, String?> {
        return getURLParameters(url, true)
    }

    fun getURLParameters(url: String, isURLEncoded: Boolean): Map<String, String?> {
        val splitted = url.substring(if (isURLEncoded) url.indexOf("?") else 0, url.length)
        val args = splitted.split("&".toRegex()).toTypedArray()
        val map: MutableMap<String, String?> = HashMap()
        if (isURLEncoded) args[0] = args[0].substring(1)
        for (arg in args) {
            val sa = arg.split("=".toRegex()).toTypedArray()
            map[sa[0]] = if (sa.size < 2) null else sa[1]
        }
        return map
    }

    /**
     *
     *
     * Checks if a CharSequence is whitespace, empty ("") or null.
     *
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
    </pre> *
     *
     * @param cs
     * the CharSequence to check, may be null
     * @return `true` if the CharSequence is null, empty or whitespace
     * @since 2.0
     * @since 3.0 Changed signature from isBlank(String) to
     * isBlank(CharSequence)
     */
    fun isBlank(cs: CharSequence?): Boolean {
        var strLen: Int = 0
        if (cs == null || cs.length.also { strLen = it } == 0) {
            return true
        }
        for (i in 0 until strLen) {
            if (!Character.isWhitespace(cs[i])) {
                return false
            }
        }
        return true
    }

    @Throws(IOException::class)
    fun executePost(conn: HttpURLConnection, req: String): String? {
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("Content-Length", req.length.toString())
        var os = conn.outputStream
        os!!.write(req.toByteArray())
        var input: DataInputStream? = DataInputStream(conn.inputStream)
        var str: String? = ""
        var response: String? = ""
        while (input!!.readLine().also { str = it } != null) {
            response += str
        }
        os.close()
        input.close()
        input = null
        os = null
        return response
    }

    fun isInteger(str: String?): Boolean {
        if (str == null) {
            return false
        }
        val length = str.length
        if (length == 0) {
            return false
        }
        var i = 0
        if (str[0] == '-') {
            if (length == 1) {
                return false
            }
            i = 1
        }
        while (i < length) {
            val c = str[i]
            if (c < '0' || c > '9') {
                return false
            }
            i++
        }
        return true
    }

    fun <C> instantiate(className: String?, vararg parameters: Any): C? {
        return try {
            val clasz = Class.forName(className) as Class<C>
            if (parameters.isNotEmpty()) {
                val paramClasses: Array<Class<*>?> = arrayOfNulls<Class<*>>(parameters.size)
                for (i in paramClasses.indices) {
                    paramClasses[i] = parameters[i].javaClass
                }
                clasz.getConstructor(*paramClasses).newInstance(*parameters)
            } else {
                clasz.getConstructor().newInstance()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fileCount(directory: String, vararg ending: String): Int {
        return fileCount(0, directory, *ending)
    }

    fun fileCount(origin: Int, directory: String, vararg ending: String): Int {
        var origin = origin
        for (f in File(directory).listFiles()!!) {
            if (f.isDirectory) {
                fileCount(origin, f.parent)
            } else {
                for (s in ending) {
                    if (f.name.toLowerCase().endsWith(s.toLowerCase())) {
                        ++origin
                        break
                    }
                }
            }
        }
        return origin
    }

    fun arrayToString(array: Array<Any>): Array<String?> {
        val str = arrayOfNulls<String>(array.size)
        for (i in array.indices) str[i] = array[i].toString()
        return str
    }

    fun <E> addAll(list: MutableList<E>, obj: Array<E>) {
        for (o in obj) list.add(o)
    }

    fun populate(from: JSONArray): Array<String?>? {
        val to = arrayOfNulls<String>(from.length())
        for (i in to.indices) {
            var `val` = from.getString(i)
            `val` = if (`val`.startsWith("/")) `val` else "/$`val`"
            to[i] = `val`
        }
        return to
    }
}
