package com.botmw.server

import io.netty.handler.ssl.SslHandler
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.Security
import java.security.cert.CertificateException
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.TrustManager

object SSLHandlerProvider {
    private const val PROTOCOL = "TLS"
    private const val ALGORITHM_SUN_X509 = "SunX509"
    private const val ALGORITHM = "ssl.KeyManagerFactory.algorithm"
    private const val KEYSTORE = "Server/mysslstore.jks"
    private const val KEYSTORE_TYPE = "JKS"
    private const val KEYSTORE_PASSWORD = "123456"
    private const val CERT_PASSWORD = "123456"
    private var serverSSLContext: SSLContext? = null
    val sSLHandler: SslHandler
        get() {
            var sslEngine: SSLEngine? = null
            if (serverSSLContext == null) {
                System.err.println("Server SSL context is null")
                System.exit(-1)
            } else {
                sslEngine = serverSSLContext!!.createSSLEngine()
                sslEngine.useClientMode = false
                sslEngine.needClientAuth = false
            }
            return SslHandler(sslEngine)
        }

    fun initSSLContext() {
        println("Initiating SSL context")
        var algorithm = Security.getProperty(ALGORITHM)
        if (algorithm == null) {
            algorithm = ALGORITHM_SUN_X509
        }
        var ks: KeyStore? = null
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(File(KEYSTORE))
            ks = KeyStore.getInstance(KEYSTORE_TYPE)
            ks.load(inputStream, KEYSTORE_PASSWORD.toCharArray())
        } catch (e: IOException) {
            System.err.println("Cannot load the keystore file")
        } catch (e: CertificateException) {
            System.err.println("Cannot get the certificate")
        } catch (e: NoSuchAlgorithmException) {
            System.err.println("Somthing wrong with the SSL algorithm")
        } catch (e: KeyStoreException) {
            System.err.println("Cannot initialize keystore")
        } finally {
            try {
                inputStream!!.close()
            } catch (e: IOException) {
                System.err.println("Cannot close keystore file stream ")
            }
        }
        try {

            // Set up key manager factory to use our key store
            val kmf = KeyManagerFactory.getInstance(algorithm)
            kmf.init(ks, CERT_PASSWORD.toCharArray())
            val keyManagers = kmf.keyManagers
            // Setting trust store null since we don't need a CA certificate or Mutual Authentication
            val trustManagers: Array<TrustManager>? = null
            serverSSLContext = SSLContext.getInstance(PROTOCOL)
            serverSSLContext!!.init(keyManagers, trustManagers, null)
        } catch (e: Exception) {
            System.err.println("Failed to initialize the server-side SSLContext")
        }
    }
}
