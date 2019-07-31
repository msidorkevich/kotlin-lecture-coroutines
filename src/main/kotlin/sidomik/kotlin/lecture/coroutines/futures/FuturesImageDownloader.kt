package sidomik.kotlin.lecture.coroutines.futures

import java.lang.Thread.sleep
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.function.Supplier

val ioExecutor: Executor = Executors.newFixedThreadPool(100) {
    val t = Thread(it)
    t.name = "io-thread"
    t
}

fun createCollage(url: URL): Array<Byte> {
    val html = downloadHtml(url)
    val imageUrls = html.thenApplyAsync { parseHtml(it) }
    val acc = emptyArray<Byte>()
    val allTasks = mutableListOf<CompletableFuture<Void>>()
    imageUrls.thenAcceptAsync { urls ->
        urls.forEach { imageUrl ->
            val image = downloadImage(imageUrl)
            image.thenAcceptAsync {
                allTasks.add(createCollage(acc, it))
            }
        }
    }
    CompletableFuture.allOf(*allTasks.toTypedArray()).join()

    return acc
}

fun downloadImagesInlined(url: URL): Array<Byte> {
    val acc = emptyArray<Byte>()
    val allTasks = mutableListOf<CompletableFuture<Void>>()
    downloadHtml(url)
        .thenApplyAsync { parseHtml(it) }
        .thenAcceptAsync { urls ->
            urls.forEach { imageUrl ->
                downloadImage(imageUrl).thenAcceptAsync { image ->
                    createCollage(acc, image)
                }
            }
        }
    CompletableFuture.allOf(*allTasks.toTypedArray()).join()

    return acc
}

fun downloadImagesWithRetry(url: URL): Array<Byte> {
    val acc = emptyArray<Byte>()
    val allTasks = mutableListOf<CompletableFuture<Void>>()
    doWithRetry({ downloadHtml(url) }, 5)
        .thenApplyAsync { parseHtml(it) }
        .thenAcceptAsync { urls ->
            urls.forEach { imageUrl ->
                doWithRetry({ downloadImage(imageUrl) }, 5)
                    .thenAcceptAsync { image ->
                        doWithRetry({ createCollage(acc, image) }, 5)
                    }
            }
        }
    CompletableFuture.allOf(*allTasks.toTypedArray()).join()

    return acc
}

fun downloadHtml(url: URL): CompletableFuture<String> =
    CompletableFuture.supplyAsync(Supplier<String> {
        println(Thread.currentThread().name + ": downloadHtml")
        sleep(100)
        "<html></html>"
    }, ioExecutor)

fun parseHtml(html: String): List<URL> {
    println(Thread.currentThread().name + ": parseHtml")
    return listOf(
        URL("https://images.unsplash.com/photo-1564277057941-13700ea13233?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=600&q=60"),
        URL("https://images.unsplash.com/photo-1564273368046-801e8c26acc5?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=600&q=60"))
}

fun downloadImage(url: URL): CompletableFuture<Array<Byte>> =
    CompletableFuture.supplyAsync(Supplier<Array<Byte>> {
        println(Thread.currentThread().name + ": downloadImage")
        sleep(500)
        arrayOf()
    }, ioExecutor)

fun createCollage(acc: Array<Byte>, image: Array<Byte>): CompletableFuture<Void> =
    CompletableFuture.runAsync(Runnable {
        println(Thread.currentThread().name + ": createCollage")
        sleep(500)
    }, ioExecutor)

fun <T> doWithRetry(action: () -> CompletableFuture<T>, retryCount: Int): CompletableFuture<T> {
    return action().handle { res: T?, error: Throwable? ->
        if (error != null) {
            if (retryCount > 1) {
                doWithRetry(action,retryCount - 1)
            } else {
                throw error
            }
        } else {
            if (res != null) {
                CompletableFuture.completedFuture(res)
            } else {
                throw IllegalArgumentException("Both result and exception are null")
            }
        }
    }.thenCompose<T> { it }
}

fun main() {
    createCollage(URL("https://unsplash.com/"))
}