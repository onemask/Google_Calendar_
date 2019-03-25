package com.example.dagger_android_ktx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector
import javax.inject.Inject

abstract class DaggerAppCompatActivity : AppCompatActivity(),HasFragmentInjector,HasSupportFragmentInjector{

    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Suppress("DEPRECATION")
    @Inject
    lateinit var frameworkFragmentInjector: DispatchingAndroidInjector<android.app.Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = supportFragmentInjector

    @Suppress("DEPRECATION")
    override fun fragmentInjector(): AndroidInjector<android.app.Fragment> = frameworkFragmentInjector

}