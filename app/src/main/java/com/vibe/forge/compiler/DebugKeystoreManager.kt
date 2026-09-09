package com.vibe.forge.compiler

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Security
import java.util.Date

/**
 * Generates a self-signed debug keystore on first use, entirely on-device
 * (no keytool needed - keytool isn't available on Android). Used only for
 * debug-signing APKs produced by BuildPipelineManager, never for release.
 */
object DebugKeystoreManager {

    private const val ALIAS = "vibeforge-debug"
    private const val PASSWORD = "android"

    init {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun getOrCreate(context: android.content.Context): File {
        val ksFile = File(ToolchainManager.binDir(context), "debug.keystore")
        if (ksFile.exists()) return ksFile

        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val now = Date()
        val expiry = Date(now.time + 30L * 365 * 24 * 60 * 60 * 1000) // 30 years, like AGP's debug.keystore
        val subject = X500Name("CN=VibeForge Debug, O=VibeForge")
        val serial = BigInteger.valueOf(now.time)

        val certBuilder = JcaX509v3CertificateBuilder(
            subject, serial, now, expiry, subject, keyPair.public
        )
        val signer = JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.private)
        val cert = JcaX509CertificateConverter().getCertificate(certBuilder.build(signer))

        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null)
        keyStore.setKeyEntry(ALIAS, keyPair.private, PASSWORD.toCharArray(), arrayOf(cert))

        ksFile.parentFile?.mkdirs()
        ksFile.outputStream().use { keyStore.store(it, PASSWORD.toCharArray()) }
        return ksFile
    }

    fun loadPrivateKeyAndCert(ksFile: File): Pair<java.security.PrivateKey, java.security.cert.X509Certificate> {
        val keyStore = KeyStore.getInstance("PKCS12")
        ksFile.inputStream().use { keyStore.load(it, PASSWORD.toCharArray()) }
        val key = keyStore.getKey(ALIAS, PASSWORD.toCharArray()) as java.security.PrivateKey
        val cert = keyStore.getCertificate(ALIAS) as java.security.cert.X509Certificate
        return key to cert
    }
}
