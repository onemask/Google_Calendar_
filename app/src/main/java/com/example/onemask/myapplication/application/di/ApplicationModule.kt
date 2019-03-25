package com.example.onemask.myapplication.application.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module

@Module
abstract class ApplicationModule {
    @Binds
    abstract fun provideContext(app : Application) : Context
}
