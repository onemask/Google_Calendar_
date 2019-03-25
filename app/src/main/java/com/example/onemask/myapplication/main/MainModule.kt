package com.example.onemask.myapplication.main

import com.example.onemask.myapplication.controller.auth.AuthFragment
import com.example.onemask.myapplication.controller.calendar.CalendarEventListFragment

import com.example.onemask.myapplication.controller.calendar.CalendarListFragment
import com.example.onemask.myapplication.scopes.FragmentScoped
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainModule{

    @FragmentScoped
    @ContributesAndroidInjector()
    abstract fun authFragement() : AuthFragment

    @FragmentScoped
    @ContributesAndroidInjector()
    abstract fun calendarListFragment() : CalendarListFragment

    @FragmentScoped
    @ContributesAndroidInjector()
    abstract fun calendarEventListFragment() : CalendarEventListFragment


}