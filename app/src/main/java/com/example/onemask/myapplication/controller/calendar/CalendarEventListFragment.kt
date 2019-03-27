package com.example.onemask.myapplication.controller.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.onemask.myapplication.R
import com.example.onemask.myapplication.repository.CalendarRepository
import com.google.api.services.calendar.model.Event
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_google_event_list.*
import javax.inject.Inject


class CalendarEventListFragment : DaggerFragment() {

    private lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var calendarRepository: CalendarRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeDisposable = CompositeDisposable()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        super.onCreateView(inflater, container, savedInstanceState)
            ?: inflater.inflate(R.layout.fragment_google_event_list, container, false)




    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let {
            val safeArgs = CalendarEventListFragmentArgs.fromBundle(it)
            if (safeArgs.calendarId != "NO_ID")
                setupCalendarDate(safeArgs.calendarId)
        }

    }

    private fun setupCalendarDate(calendarId: String) {
        getEvents(calendarId).subscribe({
            text_calendar.text = it.fold("") { acc, event ->
                acc + "date=${event.start.date} summary=${event.summary}\n"
            }
        }, {
            it.printStackTrace()
        }).apply { compositeDisposable.add(this) }
    }

    private fun getEvents(calendarId: String): Single<List<Event>> =
        calendarRepository.getEventList(calendarId)
            .observeOn(AndroidSchedulers.mainThread())

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }


}
