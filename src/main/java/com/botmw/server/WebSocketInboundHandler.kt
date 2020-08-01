package com.botmw.server

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.handler.timeout.IdleStateEvent

/**
 * @author rian
 */
class WebSocketInboundHandler(private val server: HttpServer<*>) : SimpleChannelInboundHandler<WebSocketFrame?>(false) {
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        val client = server.getClientByKey<Nothing>(ctx.channel().remoteAddress().toString()) as Client
        if (evt is IdleStateEvent) {
            if (client == null) {
                ctx.channel().writeAndFlush(CloseWebSocketFrame())
            } else {
                client.write(PingWebSocketFrame())
                client.setHasOpenPing(true)
            }
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext, frame: WebSocketFrame?) {
        val start = System.currentTimeMillis()
        var client= server.getClientByKey<Nothing>(ctx.channel().remoteAddress().toString()) as Client
        if (client == null) {
            try {
                client = server.websocketAuthenticationHandler.authenticate(server, ctx.channel(), frame) as Client
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val tt: HttpServer<Client?> = server as HttpServer<Client?>
            server.putClient("Hi", client)
            if (client != null) server.putClient(ctx.channel().remoteAddress().toString(), client) else ctx.channel().writeAndFlush(CloseWebSocketFrame())
        } else {
            try {
                server.websocketFrameHandler.handleFrame(server, client, frame)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val end = System.currentTimeMillis() - start
        println("Took " + end + "ms to process websocket")
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
    }

}
