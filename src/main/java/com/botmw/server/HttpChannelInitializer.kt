package com.botmw.server

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.traffic.AbstractTrafficShapingHandler
import io.netty.handler.traffic.ChannelTrafficShapingHandler

/**
 * @author rian
 */
class HttpChannelInitializer(private val server: HttpServer<*>, private val useSSL: Boolean) : ChannelInitializer<Channel?>() {
    @Throws(Exception::class)
    protected override fun initChannel(channel: Channel?) {
        val pipeline = channel!!.pipeline()
        if (useSSL) pipeline.addLast(SSLHandlerProvider.sSLHandler)
        pipeline.addLast("httpCodec", HttpServerCodec())
        pipeline.addLast("trafficHandler",
                ChannelTrafficShapingHandler(AbstractTrafficShapingHandler.DEFAULT_CHECK_INTERVAL))
        pipeline.addLast("gateway", GatewayHandler(server))
        pipeline.addLast("httpAggregator", HttpObjectAggregator(64 * 1024))
        pipeline.addLast("businessHandler", HttpProtocolInboundHandler(server))
    }

}
