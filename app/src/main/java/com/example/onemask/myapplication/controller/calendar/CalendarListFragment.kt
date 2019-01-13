package com.example.onemask.myapplication.controller.calendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.onemask.myapplication.R
import com.example.onemask.myapplication.R.id.layout_calendars
import com.example.onemask.myapplication.repository.CalendarRepository
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.calendar.model.CalendarListEntry
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_calendar_list.*
import javax.inject.Inject

private const val  RC_AUTH_PERMISSION = 2001

class CalendarListFragment : DaggerFragment() {

    @Inject
    lateinit var claendarRepository: CalendarRepository

    lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeDisposable = CompositeDisposable()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar_list, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showCalendarButton()
    }

    private fun showCalendarButton() {
        getCalendars().subscribe({
                layout_calendars.removeAllViews()
                addCalendarButton(it)
            }, {
                when (it) {
                    is UserRecoverableAuthIOException -> startActivityForResult(it.intent, RC_AUTH_PERMISSION)
                    else -> it.printStackTrace()
                }
            }).apply {
                compositeDisposable.add(this)
            }
    }

    private fun getCalendars() : Single<List<CalendarListEntry>> =
        claendarRepository.getCalendarList()
        .observeOn(AndroidSchedulers.mainThread())
        .map { it.items }

    private fun addCalendarButton(calendar: List<CalendarListEntry>) {
        calendar.forEach {
            layout_calendars.addView(createButton(it))
        }
    }

    private fun createButton(calendar: CalendarListEntry): Button {
        val button = Button(requireContext())
        button.text=calendar.summary
        button.setOnClickListener {
            moveToCalendarFragment(calendar.id)
        }
        return button
    }

    private fun moveToCalendarFragment(claendarId: String?) {
        CalendarListFragmentDirections.actionCalendarListToEventList().apply {
            this.calendarId=calendarId
            findNavController().navigate(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode ==Activity.RESULT_OK)
            when(requestCode){
                RC_AUTH_PERMISSION->{
                    getCalendars()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }


}







