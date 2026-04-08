package com.dhimandasgupta.funposables

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.intercept.Interceptor
import coil.memory.MemoryCache
import coil.request.ImageResult
import coil.size.Precision
import com.dhimandasgupta.funposables.di.AppGraph
import dev.zacsweers.metro.createGraph
import timber.log.Timber

class App : Application() {
    private lateinit var appGraph: AppGraph

    override fun onCreate() {
        super.onCreate()
        appGraph = createGraph<AppGraph>()
        Timber.plant(Timber.DebugTree())
        initCoil()
    }

    private fun initCoil() {
        val imageLoader = ImageLoader.Builder(this)
            .precision(Precision.INEXACT)
            .allowHardware(enable = true)
            .allowRgb565(enable = !isLowRamDevice(this))
            .components {
                add(LargeImageInterceptor())
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100 * 1024 * 1024) // 100 MB cache
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .build()

        Coil.setImageLoader(imageLoader)
    }

    fun getAppComponent(): AppGraph {
        require(::appGraph.isInitialized)
        return appGraph
    }
}

private class LargeImageInterceptor : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request

        val modifiedRequest = request.newBuilder()
            .size(request.sizeResolver)
            .precision(Precision.INEXACT)
            .build()

        return chain.proceed(modifiedRequest)
    }
}

private fun isLowRamDevice(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo().also { memoryInfo ->
        activityManager.getMemoryInfo(memoryInfo)
    }

    return memoryInfo.lowMemory
}