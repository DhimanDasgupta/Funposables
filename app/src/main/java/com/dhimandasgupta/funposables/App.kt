package com.dhimandasgupta.funposables

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.intercept.Interceptor
import coil.memory.MemoryCache
import coil.request.ImageResult
import coil.size.Precision
import com.dhimandasgupta.funposables.statemachines.CounterStateMachineFactory
import timber.log.Timber

class App : Application() {
    private var factory: CounterStateMachineFactory? = null

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        initCoil()
    }

    private fun initCoil() {
        val imageLoader = ImageLoader.Builder(this)
            .precision(Precision.INEXACT)
            .components {
                add(LargeImageInterceptor())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .build()

        Coil.setImageLoader(imageLoader)
    }

    fun getCounterStateMachine(): CounterStateMachineFactory {
        if (factory == null) {
            factory = CounterStateMachineFactory()
        }
        return factory!!
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