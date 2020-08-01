package com.botmw.server

import com.botmw.Util
import com.botmw.Watch
import com.botmw.server.http.HttpCode
import com.botmw.server.http.HttpMethod
import com.botmw.server.http.HttpProtocolVersion
import com.botmw.server.http.HttpURLRequest
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import java.lang.Boolean
import java.net.HttpCookie
import java.net.URLDecoder
import java.nio.file.Files
import java.util.*

class HttpProtocolInboundHandler internal constructor(private val server: HttpServer<*>) : SimpleChannelInboundHandler<FullHttpRequest>() {
    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        val timer = Watch()
        timer.set()
        val requestHeaders: MutableMap<String?, Any?> = HashMap()
        val headers = request.headers().entries()
        var get: Map<String, String?> = emptyMap<String, String?>()
        var post: Map<String, String?>? = emptyMap<String, String>()
        var path = URLDecoder.decode(request.uri(), "UTF-8")
        if (path.contains("?")) {
            get = Util.getURLParameters(path)
            path = path.substring(0, path.indexOf("?"))
        }

        //Paperwork..
        val protocolVersion = HttpProtocolVersion.valueOf(request.protocolVersion().toString().replace("\\.".toRegex(), "_").replaceFirst("/".toRegex(), "_"))
        var rawContent = ByteArray(0)
        if (request.method() !== io.netty.handler.codec.http.HttpMethod.GET && request.method() !== io.netty.handler.codec.http.HttpMethod.CONNECT) {
            rawContent = ByteArray(request.content().readableBytes())
            request.content().getBytes(0, rawContent)
            post = Util.getURLParameters(URLDecoder.decode(String(rawContent), "UTF-8"), false)
        }
        val httprequest = HttpURLRequest(server, ctx.channel(), protocolVersion, HttpMethod.Companion.forString(request.method().toString()), path, requestHeaders, get, post, rawContent)
        val resp = server.createResponse(httprequest)
        for ((key, value) in headers) {
            if (!key.equals(HttpHeaderNames.COOKIE.toString(), ignoreCase = true)) {
                requestHeaders[key] = value
            } else {
                val parsedCookies = HttpCookie.parse(value)
                if (!requestHeaders.containsKey("Set-Cookie")) {
                    requestHeaders["Set-Cookie"] = ArrayList<HttpCookie>()
                }
                @Suppress("UNCHECKED_CAST")
                val setCookies: MutableList<HttpCookie?>? = requestHeaders["Set-Cookie"] as  MutableList<HttpCookie?>?
               // (requestHeaders["Set-Cookie"] as List<HttpCookie?>?).addAll(parsedCookies)

                setCookies!!.addAll(parsedCookies)
            }
        }
        if (httprequest.headers!!.containsKey(HttpHeaderNames.CONTENT_TYPE.toString())) httprequest.contentType = requestHeaders[HttpHeaderNames.CONTENT_TYPE.toString()].toString()
        var cause: Exception? = null
        try {
            if (!server.routing.excludes(path)) {

                // path = path.endsWith("/") && server.routing.isAutoResolveIndex() ? path + "/index.html" : path;
                var toSend = server.resolveFile(path)
                var pathOptions = server.routing.getOptions(path)
                var withoutTrailing = path.substring(0, path.length - 1)
                withoutTrailing = withoutTrailing.substring(0, withoutTrailing.lastIndexOf('/') + 1)
                var pathToFolder = withoutTrailing
                val directoryOptions = server.routing.getOptions(pathToFolder)
                val fidx = path.indexOf("/")
                pathToFolder = path.substring(fidx, path.indexOf('/', fidx + 1) + 1)
                val directoryRootOptions = server.routing.getOptions(pathToFolder)
                val isVirtual = (pathOptions != null && pathOptions.isVirtual
                        || directoryOptions != null && directoryOptions.isVirtual
                        || directoryRootOptions != null && directoryRootOptions.isVirtual)
                if (pathOptions == null) pathOptions = directoryOptions
                if (pathOptions == null) pathOptions = directoryRootOptions
                val originalPath = path
                if (!toSend!!.exists() || toSend.isDirectory) {
                    if ((toSend.isDirectory || !toSend.exists()) && isVirtual) {
                        if (pathOptions?.virtualFileName == null) pathOptions = directoryOptions
                        if (pathOptions?.virtualFileName == null) pathOptions = directoryRootOptions
                        toSend = server.resolveFile(pathOptions?.virtualFileName!!)
                    }
                    if (!toSend.exists() && server.routing.autoResolveHtml() && !path.endsWith(".html")) toSend = server.resolveFile("$path.html")
                }
                if (!server.httpRESTHandler.handleREST(server, pathOptions, originalPath, httprequest, resp)) {
                    if (resp!!.code == HttpCode.OK && toSend.exists() && !toSend.isDirectory) {
                        resp.content = Files.readAllBytes(toSend.toPath())
                        resp.contentType = Files.probeContentType(toSend.toPath())

                        resp.contentType = if (resp.contentType == null) "text/html" else resp.contentType
                    } else if (!toSend.exists()) {
                        resp.code = HttpCode.NOT_FOUND
                    } else if (toSend.isDirectory) {
                        resp.code = HttpCode.UNAUTHORIZED
                    }
                }
            } else {
                resp!!.code = HttpCode.FORBIDDEN
            }
        } catch (ex: Exception) {
            cause = ex
            resp!!.code = HttpCode.INTERNAL_ERROR
        }
        if (resp!!.code != HttpCode.OK) {
            if (server.unsuccessfulRESTHandler != null) server.unsuccessfulRESTHandler!!.onError(server, httprequest, resp, cause)
        }
        val secureH = request.headers()["Upgrade-Insecure-Requests"]
        if (secureH != null && (Boolean.parseBoolean(secureH) || secureH == "1")) {
            println("Upgrade insecure: " + request.uri())
        }
        resp.commit()
        val end = timer.elapsedMillisD()
        println("took " + end + " milliseconds to process " + request.method() + " '" + request.uri() + "'")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
    }

}
