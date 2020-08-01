package com.botmw.server

import com.botmw.server.http.*
import com.mongodb.MongoClient
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * @author rian
 */
abstract class HttpServer<C : Client?>(configuration: JSONObject) {
    var unsuccessfulRESTHandler: UnsuccessfulRESTHandler?
    var httpRESTHandler: HttpRESTHandler
    var websocketAuthenticationHandler: WebsocketAuthenticationHandler
    var websocketFrameHandler: WebsocketFrameHandler
    protected var mongoClient: MongoClient? = null
    protected var logger = Logger.getLogger(javaClass.name)
    protected var port = 0
    private val host: String? = null
    val routing: Routing
    private var webRootFile: File? = null
    protected var clients: MutableMap<String, C>
    fun initialize(configuration: JSONObject) {
        routing.initialize(configuration.getJSONObject("routing"))
        val db = configuration.getJSONObject("database")
        val host = db.getString("host")
        val port = db.getInt("port")
        if (mongoClient != null && (mongoClient!!.address.host != host || mongoClient!!.address.port != port)) {
            mongoClient!!.close()
            mongoClient = null
        }
        if (mongoClient == null) mongoClient = MongoClient(host, port)
        this.port = configuration.getInt("port")
        webRootFile = File(routing.root)
    }

    fun resolveFile(path: String): File {
        var path = path
        if (!path.startsWith("/")) path = "/$path"
        println(webRootFile!!.exists())
        return File(webRootFile, path)
    }

    fun removeClient(key: String?): HttpServer<*> {
        val client: Client? = clients[key]
        if (client != null) {
            clients.remove(key)
        }
        return this
    }

    fun <T : C?> getClientByKey(key: String?): T? {
        return clients[key] as T?
    }

    fun putClient(key: String, client: C): HttpServer<C> {
        Objects.requireNonNull(key)
        Objects.requireNonNull(client)
        clients[key] = client
        return this
    }

    @Throws(InterruptedException::class)
    fun start(): HttpServer<*> {
        val bossGroup: EventLoopGroup = NioEventLoopGroup()
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        try {
            val insecureBootstrap = ServerBootstrap()
            insecureBootstrap.group(bossGroup, workerGroup)
            insecureBootstrap.channel(NioServerSocketChannel::class.java)
            insecureBootstrap.childHandler(HttpChannelInitializer(this, false))
            val insecureChannel = insecureBootstrap.bind(port).sync().channel()
            logger.info("Server " + insecureChannel.localAddress() + " started.")
            val secureBootstrap = ServerBootstrap()
            secureBootstrap.group(bossGroup, workerGroup)
            secureBootstrap.channel(NioServerSocketChannel::class.java)
            secureBootstrap.childHandler(HttpChannelInitializer(this, true))
            val secureChannel = secureBootstrap.bind(443).sync().channel()
            logger.info("Secure Server " + secureChannel.localAddress() + " started.")
            insecureChannel.closeFuture().sync()
            secureChannel.closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
        return this
    }

    fun createResponse(request: HttpURLRequest): HttpURLResponse {
        return HttpURLResponse(request)
    }

    init {
        routing = Routing()
        clients = ConcurrentHashMap()
        initialize(configuration)
        val adapter = HttpAdapter()
        unsuccessfulRESTHandler = adapter
        websocketAuthenticationHandler = adapter
        websocketFrameHandler = adapter
        httpRESTHandler = adapter
        SSLHandlerProvider.initSSLContext()
    }
}
