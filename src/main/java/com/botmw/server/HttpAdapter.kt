package com.botmw.server

import com.botmw.server.http.*
import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.WebSocketFrame

/**
 *
 */
class HttpAdapter : WebsocketFrameHandler, WebsocketAuthenticationHandler, HttpRESTHandler, UnsuccessfulRESTHandler {
    @Throws(Exception::class)
//    override fun handleREST(server: HttpServer<*>, options: PathOptions?, path: String?, request: HttpURLRequest?, response: HttpURLResponse?): Boolean {
//        System.err.println("Default RestHandler does not handle.")
//        println(server.resolveFile(path!!).exists())
//        println(path)
//        return !server.resolveFile(path).exists()
//    }

    override fun authenticate(server: HttpServer<*>?, channel: Channel?, webSocketFrame: WebSocketFrame?): Client? {
        throw UnsupportedOperationException("Websockets by default are unsupported.")
    }

    override fun handleFrame(server: HttpServer<*>?, client: Client?, frame: WebSocketFrame?) {
        throw UnsupportedOperationException("Websocket Frames by default are unsupported.")
    }

    override fun onError(server: HttpServer<*>?, request: HttpURLRequest, response: HttpURLResponse?, cause: Exception?) {
        if (response!!.content.toString().isEmpty()) response.content = "<html><body><p>" + response.code.digitCode + " " + response.code + " " + request.path + "</p></body></html>"
        response.contentType = "text/html"
        System.err.println("Failed to process '" + request.path + "', " + response.code.digitCode)
        cause?.printStackTrace()
    }

    override fun handleREST(server: HttpServer<*>, options: PathOptions?, path: String?, request: HttpURLRequest?, response: HttpURLResponse?): Boolean {
        println(server.resolveFile(path!!).exists())
        println(path)
        return !server.resolveFile(path).exists()
    }
}
