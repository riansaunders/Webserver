package com.botmw

import com.botmw.server.Client
import com.botmw.server.HttpServer
import org.json.JSONObject

/**
 * @author rian
 */
object Start {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val server: HttpServer<*> = object : HttpServer<Client?>(JSONObject(
    """{
                      "script-root": "/",
                      "host": "192.168.86.31",
                      "port": 80,
                      "database": {
                        "host": "localhost",
                        "port": 27018
                      },
                      "routing": {
                        "auto-resolve-html": true,
                        "auto-resolve-index": true,
                        "root": "/Users/rian/Documents/riansaunders.com/",
                        "exclude": [
                          "^(.+)(\\.(cfg|java|groovy|class|groovy|jar|xml|ini))$"
                        ],
                        "configs": [      {
                            "paths": [
                              "/",
                              "//",
                              ""
                            ],
                            "virtual": "index.html"
                          }]
                      }
                    }
        """
        )) {}
        server.start()
    }
}
