package com.botmw.server.http

import com.botmw.server.Client
import com.botmw.server.HttpServer
import io.netty.handler.codec.http.websocketx.WebSocketFrame

interface WebsocketFrameHandler {
    @Throws(Exception::class)
    fun handleFrame(server: HttpServer<*>?, client: Client?, frame: WebSocketFrame?)
}
