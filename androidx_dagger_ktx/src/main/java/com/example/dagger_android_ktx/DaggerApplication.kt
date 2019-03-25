package com.example.dagger_android_ktx

import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

abstract class DaggerApplication: DaggerApplication(), HasSupportFragmentInjector {
    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    abstract override fun applicationInjector(): AndroidInjector<out DaggerApplication>

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = supportFragmentInjector
}