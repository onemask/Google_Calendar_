package com.example.onemask.myapplication.repository

import android.content.Context
import com.example.onemask.myapplication.repository.CalendarDataService
import com.example.onemask.myapplication.repository.CalendarRepository
import com.example.onemask.myapplication.repository.CalendarService
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.util.*
import javax.inject.Singleton

@Module
abstract class GoogleCalendarDataModule {

    @Module
    companion object {
        @Singleton
        @Provides
        fun provideGoogleAccountCredential(context : Context) : GoogleAccountCredential{
            return GoogleAccountCredential
                .usingOAuth2(context, Arrays.asList(CalendarScopes.CALENDAR)
            ).setBackOff(ExponentialBackOff())
        }

        @Singleton
        @Provides
        fun provideHttpTransport() : HttpTransport{
            return AndroidHttp.newCompatibleTransport()
        }

        @Singleton
        @Provides
        fun provideJacksonfactory() : JacksonFactory {
            return JacksonFactory.getDefaultInstance()
        }

        @Singleton
        @Provides
        fun provideCalendarDataService(httpTransport: HttpTransport, jacksonfactory:JacksonFactory , googleAccountCredential: GoogleAccountCredential) : CalendarDataService{
            return CalendarDataService(httpTransport,jacksonfactory, googleAccountCredential)
        }
    }

    @Singleton
    @Binds
    abstract fun providesGooglRepository(CalendarRepository : CalendarRepository) : CalendarService

}
