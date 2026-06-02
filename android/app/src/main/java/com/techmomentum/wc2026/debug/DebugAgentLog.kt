package com.techmomentum.wc2026.debug

import com.techmomentum.wc2026.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Debug-session NDJSON ingest (emulator → host via 10.0.2.2). */
object DebugAgentLog {
    private const val ENDPOINT =
        "http://10.0.2.2:7534/ingest/41180797-19b9-4466-b775-a62b920c0ad6"
    private const val SESSION_ID = "2386b9"
    private val scope = CoroutineScope(Dispatchers.IO)

    fun log(
        location: String,
        message: String,
        hypothesisId: String,
        data: Map<String, Any?> = emptyMap(),
        runId: String = "pre-fix",
    ) {
        if (!BuildConfig.DEBUG) return
        scope.launch {
            runCatching {
                val body = JSONObject().apply {
                    put("sessionId", SESSION_ID)
                    put("location", location)
                    put("message", message)
                    put("hypothesisId", hypothesisId)
                    put("runId", runId)
                    put("timestamp", System.currentTimeMillis())
                    put("data", JSONObject(data))
                }
                val conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("X-Debug-Session-Id", SESSION_ID)
                    doOutput = true
                    connectTimeout = 2_000
                    readTimeout = 2_000
                }
                conn.outputStream.use { it.write(body.toString().toByteArray()) }
                conn.inputStream.close()
                conn.disconnect()
            }
        }
    }
}
