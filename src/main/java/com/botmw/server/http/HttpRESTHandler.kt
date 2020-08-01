package com.botmw.server.http

import com.botmw.server.HttpServer
import com.botmw.server.PathOptions

/**
 *
 */
interface HttpRESTHandler {
    @Throws(Exception::class)
    fun handleREST(server: HttpServer<*>, options: PathOptions?, path: String?, request: HttpURLRequest?, response: HttpURLResponse?): Boolean
}
