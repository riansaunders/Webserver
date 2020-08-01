package com.botmw.server.http

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.handler.codec.http.*
import io.netty.util.CharsetUtil
import java.net.HttpCookie
import java.util.*
import java.util.function.Consumer

/**
 * @author rian
 */
class HttpURLResponse(private val request: HttpURLRequest) : HttpProtocolStub() {
    var content: Any = ""
    var code = HttpCode.OK
    private val respHeaders: MutableMap<String, Any> = HashMap()
    fun destroy(): HttpURLResponse {
        return this
    }

    @Throws(Exception::class)
    fun commit(): HttpURLResponse {
        val buffer = if (content is ByteArray) Unpooled.wrappedBuffer(content as ByteArray) else Unpooled.copiedBuffer(content.toString(), CharsetUtil.UTF_8)
        val responseHeaders: HttpResponse = DefaultHttpResponse(HttpVersion.valueOf(request.protocol!!.value),
                HttpResponseStatus.valueOf(code.digitCode))
        responseHeaders.headers()[HttpHeaderNames.CONTENT_TYPE] = contentType
        val headers = getRespHeaders()
        headers?.keys?.forEach(Consumer { key: String ->
            val entry = headers[key]
            if (entry !is List<*>) responseHeaders.headers().add(key, entry) else {
                for (o in entry) {
                    responseHeaders.headers().add(key, o.toString())
                }
            }
        })
        val connectionHeader = request.headers!!.getOrDefault("Connection", "").toString()
        val keepAlive = connectionHeader != null && connectionHeader.equals("keep-alive", ignoreCase = true)
        if (keepAlive) {
            responseHeaders.headers()[HttpHeaderNames.CONTENT_LENGTH] = buffer.readableBytes()
            responseHeaders.headers()[HttpHeaderNames.CONNECTION] = HttpHeaderValues.KEEP_ALIVE
        }

        // write response
        request.channel.write(responseHeaders)
        request.channel.write(buffer)
        val future = request.channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
        if (!keepAlive) future.addListener(ChannelFutureListener.CLOSE)
        return this
    }

    fun putClientCookie(value: HttpCookie): HttpURLResponse {
        value.isHttpOnly = true
        putHeader("Set-Cookie", stringify(value))
        return this
    }

    fun removeCookie(name: String?): HttpURLResponse {
        val cookie = HttpCookie(name, "")
        cookie.maxAge = 0
        return putClientCookie(cookie)
    }

    fun redirect(url: String): HttpURLResponse {
        code = HttpCode.FOUND
        putHeader("Location", url)
        return this
    }

    fun putHeader(name: String, value: String): HttpProtocolStub {
        respHeaders[name] = value
        return this
    }

    fun getRespHeaders(): Map<String, Any> {
        return respHeaders
    }

    companion object {
        private fun stringify(cookie: HttpCookie): String {
            val sb = StringBuilder()
            sb.append(cookie.name).append("=").append(cookie.value)
            if (cookie.path != null) sb.append("; Path=").append(cookie.path)
            if (cookie.domain != null) sb.append("; Domain=").append(cookie.domain)
            if (cookie.portlist != null) sb.append("; Port=").append(cookie.portlist)
            sb.append("; Max-Age=").append(cookie.maxAge.toString() + "")
            return sb.toString()
        }
    }

}
