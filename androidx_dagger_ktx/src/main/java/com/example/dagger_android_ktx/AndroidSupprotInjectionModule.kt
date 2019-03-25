package com.example.dagger_android_ktx

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.multibindings.Multibinds

@Module(includes = [AndroidInjection::class])
abstract class AndroidSupprotInjectionModule private constructor(){
    @Multibinds
    internal abstract fun supportFragmentInjectorFactories(): Map<Class<out Fragment>,AndroidInjector.Factory<out Fragment>>

    @Multibinds
    internal abstract fun supportFragmentInjectorFactoriesWithStringKeys(): Map<String, AndroidInjector.Factory<out Fragment>>}