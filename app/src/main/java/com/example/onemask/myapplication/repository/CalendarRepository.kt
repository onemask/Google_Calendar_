package com.example.onemask.myapplication.repository




import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import io.reactivex.Single


class CalendarRepository  constructor (private val remote : CalendarDataService) : CalendarService {
    override fun getCalendarList(): Single<CalendarList> {
        return remote.getCalendarList()
    }
    override fun getEventList(calendarId: String): Single<List<Event>> {
        return remote.getEventList(calendarId)
    }

}
