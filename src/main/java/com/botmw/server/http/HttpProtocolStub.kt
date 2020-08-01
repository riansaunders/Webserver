package com.botmw.server.http

/**
 *
 * @author rian
 */
abstract class HttpProtocolStub @JvmOverloads constructor(val headers: Map<String?, Any?>? = null) {
    var contentType = "text/html"
    var protocol: HttpProtocolVersion? = null
        private set

    fun setProtocolVersion(version: HttpProtocolVersion?): HttpProtocolStub {
        protocol = version
        return this
    }

}
