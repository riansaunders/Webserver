package com.botmw.server

import org.json.JSONObject

/**
 * @author rian
 */
class PathOptions {

    val isVirtual: Boolean
        get() = virtualFileName != null && !virtualFileName!!.isEmpty()

    var isAuthRequired = false
        private set
    var isDeauthRequired = false
        private set
    var virtualFileName: String? = null
        private set

    fun initialize(config: JSONObject) {
        virtualFileName = if (config.has("virtual")) config.getString("virtual") else null
        isAuthRequired = config.has("auth-required") && config.getBoolean("auth-required")
        isDeauthRequired = config.has("deauth-required") && config.getBoolean("deauth-required")
    }

    override fun toString(): String {
        return "PathOptions{" +
                "authRequired=" + isAuthRequired +
                ", deauthRequired=" + isDeauthRequired +
                ", virtualFileName='" + virtualFileName + '\'' +
                '}'
    }
}
