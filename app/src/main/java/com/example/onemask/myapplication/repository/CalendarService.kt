package com.example.onemask.myapplication.repository

import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import io.reactivex.Single


interface  CalendarService {
    fun getCalendarList() : Single<CalendarList>
    fun getEventList(calendarId: String): Single<List<Event>>
}