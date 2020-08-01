package com.botmw.server.http

import com.botmw.server.HttpServer
import io.netty.channel.Channel
import java.net.HttpCookie
import java.net.SocketAddress

/**
 * @author rian
 */
class HttpURLRequest(val server: HttpServer<*>, val channel: Channel, version: HttpProtocolVersion?, val method: HttpMethod?, val path: String, headers: Map<String?, Any?>?, val get: Map<String, String?>?, val post: Map<String, String?>?, val body: ByteArray) : HttpProtocolStub(headers) {
    val remoteAddress: SocketAddress
    fun getCookie(name: String): HttpCookie? {
        val entry = headers!!["Set-Cookie"] ?: return null
        for (cookie in entry as List<HttpCookie>) {
            if (cookie.name == name) return cookie
        }
        return null
    }

    fun destroy(): HttpURLRequest {
        channel.close()
        return this
    }

    init {
        setProtocolVersion(version)
        remoteAddress = channel.remoteAddress()
    }
}
