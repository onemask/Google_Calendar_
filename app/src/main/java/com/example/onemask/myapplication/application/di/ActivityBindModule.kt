package com.example.onemask.myapplication.application.di

import com.example.onemask.myapplication.main.MainActivity
import com.example.onemask.myapplication.main.MainModule
import com.example.onemask.myapplication.scopes.ActivityScoped
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindModule {
    @ActivityScoped
    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun mainActivity()  : MainActivity
}
