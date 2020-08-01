package com.botmw.server

import com.botmw.server.http.HttpURLRequest
import com.botmw.server.http.HttpURLResponse

/**
 * @author rian
 */
interface UnsuccessfulRESTHandler {
    fun onError(server: HttpServer<*>?, request: HttpURLRequest, response: HttpURLResponse?, cause: Exception?)
}
