package com.botmw.server

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame

class Client(protected val channel: Channel) {
    private var hasOpenPing = false
    fun setHasOpenPing(hasOpenPing: Boolean) {
        this.hasOpenPing = hasOpenPing
    }

    fun hasOpenPing(): Boolean {
        return hasOpenPing
    }

    fun write(message: Any): ChannelFuture {
        return if (message is ByteArray) {
            channel.writeAndFlush(BinaryWebSocketFrame(Unpooled.wrappedBuffer(message)))
        } else if (message is WebSocketFrame) {
            channel.writeAndFlush(message)
        } else {
            channel.writeAndFlush(TextWebSocketFrame(message.toString()))
        }
    }

    fun close(): ChannelFuture {
        return write(CloseWebSocketFrame())
    }

}
