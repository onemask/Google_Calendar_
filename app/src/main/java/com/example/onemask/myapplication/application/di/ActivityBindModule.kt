package com.example.onemask.myapplication.application.di

import com.example.onemask.myapplication.MainActivity
import com.example.onemask.myapplication.scopes.ActivityScoped
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindModule {
    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun mainActivity()  : MainActivity
}
