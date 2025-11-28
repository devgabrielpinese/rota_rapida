// app/src/main/java/com/example/rota_rapida/di/RepositoryModule.kt
package com.example.rota_rapida.di

import com.example.rota_rapida.data.repository.RouteRepository
import com.example.rota_rapida.data.repository.RouteRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRouteRepository(
        impl: RouteRepositoryImpl
    ): RouteRepository
}
