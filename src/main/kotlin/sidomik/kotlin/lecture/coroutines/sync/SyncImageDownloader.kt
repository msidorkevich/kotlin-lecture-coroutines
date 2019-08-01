package sidomik.kotlin.lecture.coroutines.sync

import java.lang.Thread.sleep
import java.net.URL

fun createCollage(url: URL) {
    val acc = emptyArray<Byte>()

    val html = downloadHtml(url)
    val imageUrls = parseHtml(html)

    imageUrls.forEach { imageUrl ->
        val image = downloadImage(imageUrl)
        createCollage(acc, image)
    }
}

fun createCollageWithRetry(url: URL) {
    val acc = emptyArray<Byte>()

    val html = retry({ downloadHtml(url) }, 5)
    val imageUrls = retry({ parseHtml(html) }, 5)

    imageUrls.forEach { imageUrl ->
        val image = retry({ downloadImage(imageUrl) }, 5)
        createCollage(acc, image)
    }
}

fun <T> retry(action: () -> T, retryCount: Int): T {
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

fun downloadHtml(url: URL): String {
    sleep(100)
    return "<html></hmtl>"
}

fun parseHtml(html: String): List<URL> = emptyList()

fun downloadImage(url: URL): Array<Byte> {
    sleep(500)
    return arrayOf()
}

fun createCollage(acc: Array<Byte>, image: Array<Byte>) {
    sleep(500)
}