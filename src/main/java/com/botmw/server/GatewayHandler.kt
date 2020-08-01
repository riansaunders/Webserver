package com.botmw.server

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.concurrent.Future

class GatewayHandler(private val server: HttpServer<*>) : ChannelInboundHandlerAdapter() {
    private var handshaker: WebSocketServerHandshaker? = null
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is HttpRequest) {
            val httpRequest = msg
            val headers = httpRequest.headers()
            if ("Upgrade".equals(headers[HttpHeaderNames.CONNECTION], ignoreCase = true) &&
                    "WebSocket".equals(headers[HttpHeaderNames.UPGRADE], ignoreCase = true)) {
                ctx.pipeline().remove("businessHandler")
                ctx.pipeline().remove("httpAggregator")
                ctx.pipeline().addLast("compressionHandler", WebSocketServerCompressionHandler())
                ctx.pipeline().addLast("idleStateHandler", IdleStateHandler(0, 0, 30))
                ctx.pipeline().addLast("businessHandler", WebSocketInboundHandler(server))
                ctx.channel().closeFuture().addListener { l: Future<in Void?>? -> server.removeClient(ctx.channel().remoteAddress().toString()) }

                //Do the Handshake to upgrade connection from HTTP to WebSocket protocol
                handleHandshake(ctx, httpRequest)
            }
        } else {
            println("Incoming request is unknown - $msg")
        }
        ctx.pipeline().remove("gateway")
        ctx.fireChannelRead(msg)
    }

    protected fun handleHandshake(ctx: ChannelHandlerContext, req: HttpRequest) {
        val wsFactory = WebSocketServerHandshakerFactory(getWebSocketURL(req), null, true)
        handshaker = wsFactory.newHandshaker(req)
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel())
        } else {
            handshaker!!.handshake(ctx.channel(), req)
        }
    }

    protected fun getWebSocketURL(req: HttpRequest): String {
        return "websocket://" + req.headers()["Host"] + req.uri()
    }

}
