package com.example.onemask.myapplication.controller.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.onemask.myapplication.R
import com.example.onemask.myapplication.repository.CalendarRepository
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.TimePeriod
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_google_event_list.*
import timber.log.Timber
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
        setHasOptionsMenu(true)
        arguments?.let {
            CalendarEventListFragmentArgs.fromBundle(it).let { args ->
                if (args.calendarId != "NO_ID")
                //3. presenter 인터페이스 함수를 호출한다.
                    getEvents(args.calendarId)
            }
        }

    }

    /**
     * 1. view 인터페이스를 정의한다.
     * 2. presenter 인터페이스를 정의한다.
     */


    // 4. callback 함수를 정의한다.
    public fun setEvents(event: List<Event>?) {
        event?.let {
            text_calendar.text = createEventsText(it)
        }
    }

    /*// todo: move method in presenter
    // compositeDisposable
    private fun setupCalendarDate(calendarId: String) {
        getEvents(calendarId).subscribe({
            text_calendar.text = it.fold("") { acc, event ->
                acc + "date=${event.start.date} summary=${event.summary}\n"
            }
        }, {
            it.printStackTrace()
        }).apply { compositeDisposable.add(this) }
    }*/



    //todo: move method in presenter
    private fun getEvents(calendarId: String) {
        calendarRepository.getEventList(calendarId)
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { progress_loading?.visibility = View.GONE }
            .subscribe({
                Timber.d("getEvents ${Thread.currentThread()}")
                setEvents(it)
            }, {
                it.printStackTrace()
            }).apply { compositeDisposable.add(this) }
    }

    private fun createEventsText(events: List<Event>): String {
        val size = events.size
        return events.foldIndexed("") { index, acc, event ->
            acc + "date=${event.start.date} summary=${event.summary}" + if (index == size - 1) "" else "\n"
        }
    }

    override fun onDestroy() {
        if (!compositeDisposable.isDisposed)
            compositeDisposable.dispose()
        super.onDestroy()
    }


}
