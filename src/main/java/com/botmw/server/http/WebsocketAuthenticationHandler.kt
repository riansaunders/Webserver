package com.botmw.server.http

import com.botmw.server.Client
import com.botmw.server.HttpServer
import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.WebSocketFrame

interface WebsocketAuthenticationHandler {
    @Throws(Exception::class)
    fun authenticate(_server: HttpServer<*>?, _channel: Channel?, frame: WebSocketFrame?): Client?
}
