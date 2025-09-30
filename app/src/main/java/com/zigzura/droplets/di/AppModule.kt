package com.zigzura.droplets.di

import android.content.Context
import com.zigzura.droplets.api.ApiClient
import com.zigzura.droplets.api.ClaudeApiService
import com.zigzura.droplets.data.PreferencesManager
import com.zigzura.droplets.repository.ClaudeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideClaudeApiService(): ClaudeApiService {
        return ApiClient.claudeApiService
    }

    @Provides
    @Singleton
    fun provideClaudeRepository(
        preferencesManager: PreferencesManager
    ): ClaudeRepository {
        return ClaudeRepository(preferencesManager)
    }
}
