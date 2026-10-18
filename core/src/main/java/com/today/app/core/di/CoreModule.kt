package com.today.app.core.di

import androidx.room.Room
import com.today.app.core.BuildConfig
import com.today.app.core.data.CatalogRepository
import com.today.app.core.data.DiaryRepository
import com.today.app.core.data.source.local.DiaryLocalDataSource
import com.today.app.core.data.source.local.LocalDataSource
import com.today.app.core.data.source.local.room.TitleDatabase
import com.today.app.core.data.source.remote.RemoteDataSource
import com.today.app.core.data.source.remote.network.ApiService
import com.today.app.core.domain.repository.ICatalogRepository
import com.today.app.core.domain.repository.IDiaryRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val databaseModule = module {
    factory { get<TitleDatabase>().titleDao() }
    factory { get<TitleDatabase>().diaryDao() }
    factory { get<TitleDatabase>().watchlistDao() }
    factory { get<TitleDatabase>().customListDao() }
    single {
        Room.databaseBuilder(
            androidContext(),
            TitleDatabase::class.java, "Today.db"
        ).fallbackToDestructiveMigration().build()
    }
}

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val originalUrl = original.url

                val newUrl = originalUrl.newBuilder()
                    .addQueryParameter("api_key", BuildConfig.API_KEY)
                    .build()

                val request = original.newBuilder()
                    .url(newUrl)
                    .addHeader("Authorization", "Bearer ${BuildConfig.BEARER_TOKEN}")
                    .addHeader("Accept", "application/json")
                    .build()

                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }
    single {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
        retrofit.create(ApiService::class.java)
    }
}

val repositoryModule = module {
    single { LocalDataSource(get()) }
    single { RemoteDataSource(get()) }
    single<ICatalogRepository> { CatalogRepository(get(), get(), get()) }
    single { DiaryLocalDataSource(get(), get(), get()) }
    single<IDiaryRepository> { DiaryRepository(get()) }
}
