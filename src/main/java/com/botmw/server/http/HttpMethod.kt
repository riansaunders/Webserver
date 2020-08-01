package com.botmw.server.http

/**
 *
 * @author rian
 */
enum class HttpMethod(private val nameInString: String) {
    POST("post"), GET("get"), DELETE("delete"), PUT("put"), PATCH("patch");

    companion object {
        fun forString(name: String?): HttpMethod? {
            for (method in values()) if (method.nameInString.equals(name, ignoreCase = true)) return method
            return null
        }
    }

}
