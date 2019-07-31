package sidomik.kotlin.lecture.coroutines.coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import sidomik.kotlin.lecture.coroutines.futures.createCollage
import java.net.URL

fun downloadImages(url: URL) {
    val acc = emptyArray<Byte>()

    runBlocking {
        val html = downloadHtml(url)
        val imageUrls = parseHtml(html)

        imageUrls
            .map { async { downloadImage(it) } }
            .forEach { createCollage(acc, it.await()) }
    }
}

suspend fun downloadImagesWithRetry(url: URL) {
    val acc = emptyArray<Byte>()

    val html = retry({ downloadHtml(url) }, 5)
    val imageUrls = retry({ parseHtml(html) }, 5)

    imageUrls.forEach { imageUrl ->
        val image = retry({ downloadImage(imageUrl) }, 5)
        createCollage(acc, image)
    }
}

suspend fun <T> retry(action: suspend () -> T, retryCount: Int): T {
    var i = 0
    while (i < retryCount - 1) {
        try {
            return action.invoke()
        } catch (e: Exception) {
            i++
        }
    }
    return action.invoke()
}

suspend fun downloadHtml(url: URL): String {
    delay(100)
    return "<html></hmtl>"
}

fun parseHtml(html: String): List<URL> = emptyList()

suspend fun downloadImage(url: URL): Array<Byte> {
    delay(500)
    return arrayOf()
}

fun createCollage(acc: Array<Byte>, image: Array<Byte>) {
    Thread.sleep(10)
}

fun main() {
    createCollage(URL("https://unsplash.com/"))
}
