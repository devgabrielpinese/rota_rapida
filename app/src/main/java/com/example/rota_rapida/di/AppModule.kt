package com.example.rota_rapida.di

import android.content.Context
import com.example.rota_rapida.data.input.GeocodingService
import com.example.rota_rapida.data.input.ManualInputService
import com.example.rota_rapida.data.location.LocationProvider
import com.example.rota_rapida.data.remote.RotaRapidaApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ===== LOCATION =====
    @Provides @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @Provides @Singleton
    fun provideLocationProvider(
        @ApplicationContext context: Context
    ): LocationProvider = LocationProvider(context)

    // ===== NETWORK =====
    @Provides @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            chain.proceed(
                original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build()
            )
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.rotarapida.com/v1/") // ajuste quando o backend estiver pronto
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides @Singleton
    fun provideRotaRapidaApi(retrofit: Retrofit): RotaRapidaApi =
        retrofit.create(RotaRapidaApi::class.java)

    // ===== DATA SOURCES / AUX =====
    @Provides @Singleton
    fun provideGeocodingService(
        @ApplicationContext context: Context
    ): GeocodingService = GeocodingService(context) // se você ainda usa esta classe

    @Provides @Singleton
    fun provideManualInputService(
        @ApplicationContext context: Context
    ): ManualInputService = ManualInputService(context)

    // ❌ IMPORTANTE: não tenha aqui nenhum `@Provides RouteRepository`.
    // O binding do RouteRepository deve ficar SOMENTE no RepositoryModule via @Binds.
}
