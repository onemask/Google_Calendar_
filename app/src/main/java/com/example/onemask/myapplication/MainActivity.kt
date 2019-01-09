package com.example.onemask.myapplication

import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.onemask.myapplication.repository.CalendarDataService
import com.example.onemask.myapplication.repository.CalendarRepository


import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.hasPermissions
import java.util.*

private const val REQUEST_PERMISSION_GET_ACCOUNTS = 1001
private const val REQUEST_CODE_PLAY_SERVICE = 1002
private const val REQUEST_ACCOUNT_PICKER = 1003
private const val REQUEST_AUTHORIZATION = 1004
private const val RC_AUTH_PERMISSION = 1005


class MainActivity : AppCompatActivity() {

    private lateinit var googleAccountCredential: GoogleAccountCredential
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var googleCalendarRepository: CalendarRepository
    private lateinit var calendarDataService: CalendarDataService

    private var REQUEST_ACCOUNT: String = "accountName"
    private var calendarId: String = "onemask14@gmail.com"
    private var isgoogle: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_auth.setOnClickListener {
            getAuth()
        }

    }

    fun getAuth() {
        val httptransport: HttpTransport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JacksonFactory = JacksonFactory.getDefaultInstance()

        //구글 인증 관련.
        compositeDisposable = CompositeDisposable()

        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
            applicationContext, Arrays.asList(CalendarScopes.CALENDAR)
        ).setBackOff(ExponentialBackOff())

        calendarDataService = CalendarDataService(httptransport, jsonFactory, googleAccountCredential)
        googleCalendarRepository = CalendarRepository(calendarDataService)


        isGooglePlayServiceAvailable()

        if(isgoogle){
            button_holiday.setOnClickListener {
                getCalendarList()
            }
            button_month.setOnClickListener {
                getEventList(calendarId)
            }
        }
        else{
            acquireGooglePlayServices()
        }
    }

    private fun getCalendarList() {

        googleCalendarRepository.getCalendarList()
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.items }
            .subscribe({
                it.forEach { item ->
//                    val button = Button(this)
//                    button.text=item.summary
//                    button.setOnClickListener {
//                        getEventList(item.id)
//                      }
//                    layout_button.addView(button)
                    getEventList(item.id)
                    Log.d("지금 나오는 item.id",item.id)
                }
            }, {
                when (it) {
                    is UserRecoverableAuthIOException -> startActivityForResult(it.intent, RC_AUTH_PERMISSION)
                    else -> it.printStackTrace()
                }
            }).apply { compositeDisposable.add(this) }

    }

    private fun getEventList(calendarId: String) {
            googleCalendarRepository.getEventList(calendarId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    text_field.text = it.fold("") { acc, event ->
                        acc + "date=${event.start.date} summary=${event.summary}\n"
                    }
                }, {
                    when (it) {
                        is UserRecoverableAuthIOException -> startActivityForResult(it.intent, RC_AUTH_PERMISSION)
                        else -> it.printStackTrace()
                    }
                })
                .apply {
                    compositeDisposable.add(this)
                }

    }


    private fun isGooglePlayServiceAvailable(): Boolean {
        googleAccountCredential.selectedAccountName?.let {
            Toast.makeText(this, "구글 인증이 되어있습니다.", Toast.LENGTH_SHORT).show()
            isgoogle = true
            button_auth.visibility = View.INVISIBLE
            button_month.visibility = View.VISIBLE
            button_holiday.visibility = View.VISIBLE
            return true
        }.let {
            Toast.makeText(this, "구글 인증을 선택해주세요.", Toast.LENGTH_SHORT).show()
            isgoogle = false
            choseAccount()
            return false
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun choseAccount() {
        // GET_ACCOUNTS 권한 O
        if (hasPermissions(this, android.Manifest.permission.GET_ACCOUNTS)) {
            val accountName: String? = getPreferences(Context.MODE_PRIVATE).getString(REQUEST_ACCOUNT, null)
            accountName?.let {
                googleAccountCredential.selectedAccountName = accountName
            }.let {
                startActivityForResult(googleAccountCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // 사용자에게 GET_ACCOUNTS 권한을 요구하는 다이얼로그를 보여준다.(주소록 권한 요청함)
            EasyPermissions.requestPermissions(
                this,
                "구글 계정 권한이 필요합니다.",
                REQUEST_PERMISSION_GET_ACCOUNTS,
                android.Manifest.permission.GET_ACCOUNTS
            )
        }
    }


    private fun acquireGooglePlayServices() {
        val availability: GoogleApiAvailability = GoogleApiAvailability.getInstance()
        val state = availability.isGooglePlayServicesAvailable(this)
        if (availability.isUserResolvableError(state)) {
            showErrorDialog(state);
        }
    }

    private fun showErrorDialog(state: Int) {
        val availability: GoogleApiAvailability = GoogleApiAvailability.getInstance()
        val dialog: Dialog = (availability.getErrorDialog(this, state, REQUEST_CODE_PLAY_SERVICE))
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PLAY_SERVICE -> {
                    text_field.text = "구글 플레이 서비스를 설치 해주세요."
                    choseAccount()
                }
                REQUEST_ACCOUNT_PICKER -> {
                    val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)

                    accountName?.let {
                        getPreferences(Context.MODE_PRIVATE).edit().putString(REQUEST_ACCOUNT, it).apply()
                    }.let {
                        Toast.makeText(this, "뱅글뱅글", Toast.LENGTH_SHORT).show()
                        return choseAccount()
                    }
                }
                REQUEST_AUTHORIZATION -> {
                    choseAccount()
                }
                RC_AUTH_PERMISSION -> {
                    Toast.makeText(this, "구글 인증이 필요합니다.", Toast.LENGTH_SHORT).show()
                    choseAccount()
                }

            }
        }
    }
}




