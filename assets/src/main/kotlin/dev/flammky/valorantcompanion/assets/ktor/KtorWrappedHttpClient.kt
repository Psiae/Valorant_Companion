package dev.flammky.valorantcompanion.assets.ktor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import androidx.core.content.FileProvider
import dev.flammky.valorantcompanion.assets.ReadableAssetByteChannel
import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.http.AssetHttpResponse
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.client.HttpClient as KtorHttpClient

class KtorWrappedHttpClient(
    private val self: KtorHttpClient
) : AssetHttpClient() {

    // TODO: limit download, simply prepare custom execute block, the call will be closed once it returns
    override suspend fun get(url: String): AssetHttpResponse {
        val response = self.get(url) {
            onDownload { bytesSentTotal, contentLength ->
                Log.d("KtorWrappedHttpClient", "get($url), downloaded$bytesSentTotal, contentLength=$contentLength")
            }
        }
        val content = response.bodyAsChannel()
        Log.d("assets.ktor.KtorWrappedHttpClient", "get($url), status=${response.status}")
        return AssetHttpResponse(
            statusCode = response.status.value,
            contentChannel = ReadableAssetByteChannel { bb ->
                while (true) {
                    if (content.readAvailable(bb) == -1) break
                }
            }
        )
    }
}