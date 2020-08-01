package com.botmw.server

import com.botmw.Util
import org.json.JSONObject
import java.util.*

/**
 * @author rian
 */
class Routing {
    var isAutoResolveIndex = false
        private set
    private var autoResolveHtml = false
    var root: String? = null
        private set
    private val pathOptionsMap: MutableMap<String?, PathOptions?>
    private var exclude: Array<String?>? = null

    fun initialize(configuration: JSONObject) {
        pathOptionsMap.clear()
        root = configuration.getString("root")
        var excludesArray = configuration.getJSONArray("exclude")
        exclude = if (configuration.has("exclude")) Util.populate(excludesArray) else null
        autoResolveHtml = configuration.getBoolean("auto-resolve-html")
        isAutoResolveIndex = configuration.has("auto-resolve-index") && configuration.getBoolean("auto-resolve-index")
        val configArray = configuration.getJSONArray("configs")
        for (i in 0 until configArray.length()) {
            val config = configArray.getJSONObject(i)
            val paths = Util.populate(config.getJSONArray("paths"))
            var pathOptions: PathOptions? = null
            for (path in paths!!) {
                pathOptions = pathOptionsMap[path]
                if (pathOptions == null) pathOptionsMap[path] = pathOptions ?: PathOptions().also { pathOptions = it }
            }
            pathOptions!!.initialize(config)
            println(Arrays.toString(paths) + " = " + pathOptions)
        }
    }

    fun excludes(path: String): Boolean {
        if (exclude == null) return false
        for (exclusion in exclude!!) {
            if (path.matches(exclusion!!.toRegex())) return true
        }
        return false
    }

    fun getOptions(path: String): PathOptions? {
        for (key in pathOptionsMap.keys) {
            if (key == path || path.matches(key!!.toRegex())) return pathOptionsMap[key]
        }
        return null
    }

    fun autoResolveHtml(): Boolean {
        return autoResolveHtml
    }

    init {
        pathOptionsMap = HashMap()
    }
}
