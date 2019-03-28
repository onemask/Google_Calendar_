package com.example.onemask.myapplication.repository.remote

import android.util.Log
import com.example.onemask.myapplication.repository.CalendarService
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class CalendarDataService @Inject constructor(
    httptransport: HttpTransport,
    jacksonFactory: JacksonFactory,
    googleAccountCredential: GoogleAccountCredential
) : CalendarService {

    private val calendar: Calendar = Calendar.Builder(httptransport, jacksonFactory, googleAccountCredential)
        .setApplicationName("Google Calendar using by Dagger ")
        .build()

    override fun getCalendarList(): Single<CalendarList> {
        return Single.fromCallable {
            // test Schedulers.io() 인지 main thread 인지
            Timber.d("thread ${Thread.currentThread()}")
            calendar.CalendarList().list().execute()
        }
            .subscribeOn(Schedulers.io())
            .map {
                Timber.d(" in io Scheduler ${Thread.currentThread()}")
                it
            }
    }

    override fun getEventList(calendarId: String): Single<List<Event>> {
        return Single.fromCallable {
            calendar.events()
                .list(calendarId)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
        }
            .subscribeOn(Schedulers.io())
            .map { it.items }
    }

}


/*override fun getCalendarList(): Single<CalendarList> {
    return Single.fromCallable {
        // test Schedulers.io() 인지 main thread 인지
        Log.d("!!!!", "Thread ${Thread.currentThread()}")
        calendar.CalendarList().list().execute()
    }
        .subscribeOn(Schedulers.io())
        .map {
            Log.d("!!!!", "Thread-2 ${Thread.currentThread()}")
            it
        }
}*/






