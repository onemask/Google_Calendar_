package com.example.dagger_android_ktx

import android.content.Context
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

abstract class DaggerAppCompatDialogFragment : AppCompatDialogFragment(),HasSupportFragmentInjector {

    @Suppress("MemberVisibilityCanBePrivate")
    @Inject
    protected lateinit var childFragmentInjector : DispatchingAndroidInjector<Fragment>

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = childFragmentInjector

}