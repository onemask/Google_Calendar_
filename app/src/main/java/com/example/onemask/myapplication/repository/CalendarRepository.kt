package com.example.onemask.myapplication.repository

import com.example.onemask.myapplication.repository.remote.CalendarDataService
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import io.reactivex.Single
import javax.inject.Inject

class CalendarRepository @Inject constructor (private val remote : CalendarDataService) : CalendarService {

    override fun getCalendarList(): Single<CalendarList> = remote.getCalendarList()
    override fun getEventList(calendarId: String): Single<List<Event>> = remote.getEventList(calendarId)
}
