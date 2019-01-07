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

    private val REQUEST_ACCOUNT: String = "accountName"
    private var calendarId: String = "onemask14@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getAuth()
        makeObject()

        add_calendar.setOnClickListener {
            getEventList(calendarId)
        }
    }

    fun getAuth(){
        //구글 인증 관련.
        compositeDisposable = CompositeDisposable()
        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
            applicationContext, Arrays.asList(CalendarScopes.CALENDAR)
        ).setBackOff(ExponentialBackOff())
    }

    fun makeObject() {
        val httptransport: HttpTransport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JacksonFactory = JacksonFactory.getDefaultInstance()
        calendarDataService = CalendarDataService(httptransport, jsonFactory, googleAccountCredential)
        googleCalendarRepository = CalendarRepository(calendarDataService)

    }

    private fun CalendarList() {
        if (isGooglePlayServiceAvailable()) {
            googleCalendarRepository.getCalendarList()
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.items }
                .subscribe({
                    it.forEach { item ->
                        val button = Button(this)
                        text_field.text = item.summary
                        add_calendar.setOnClickListener {
                            getEventList(item.id)
                        }
                    }
                }, {
                    when (it) {
                        is UserRecoverableAuthIOException -> startActivityForResult(it.intent, RC_AUTH_PERMISSION)
                        else -> it.printStackTrace()
                    }
                }).apply { compositeDisposable.add(this) }
        }
    }


    private fun getEventList(calendarId: String) {
        if (isGooglePlayServiceAvailable()) {
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
    }

    private fun getResultFromApi() {
        //google play service 이용 불가능
        if (!isGooglePlayServiceAvailable()) {
            acquireGooglePlayServices()
        } else {
            googleAccountCredential.selectedAccountName?.let {
                CalendarList()
            }.let {
                //choseAccount()
            }
        }
    }

    private fun isGooglePlayServiceAvailable(): Boolean {
        var test: Boolean = true

        googleAccountCredential.selectedAccountName?.let {
            Log.d("구글인증이 가능하다.", test.toString())
            return true
        }.let {
            test = false
            Log.d("구글인증이 불가능하다.", test.toString())
            choseAccount()
            return false
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun choseAccount() {
        // GET_ACCOUNTS 권한 O
        if (hasPermissions(this, android.Manifest.permission.GET_ACCOUNTS)) {

            // SharedPreferences에서 저장된 Google 계정 이름을 가져온다.
            val accountName: String? = getPreferences(Context.MODE_PRIVATE).getString(REQUEST_ACCOUNT, null)
            accountName?.let {
                // 선택된 구글 계정 이름으로 설정한다.
                googleAccountCredential.selectedAccountName = accountName
                CalendarList();
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

        val accountName = AccountManager.KEY_ACCOUNT_NAME

        when (requestCode) {
            REQUEST_CODE_PLAY_SERVICE -> {
                if (resultCode == Activity.RESULT_OK) {
                    text_field.text = "구글 플레이 서비스를 설치 해주세요."
                } else
                    getResultFromApi()
            }
            REQUEST_ACCOUNT_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val accountName: String = data!!.getStringExtra(accountName)
                    accountName?.let {
                        Log.d("나는 멍청이 ", accountName)

                        val setting = getPreferences(Context.MODE_PRIVATE)
                        val edittor: SharedPreferences.Editor =
                            setting.edit().putString(REQUEST_ACCOUNT, accountName).apply {
                                googleAccountCredential.setSelectedAccountName(accountName)?.let {
                                    getResultFromApi()
                                    apply()
                                }
                            }
                    }
                }
            }
            REQUEST_AUTHORIZATION -> {
                if (requestCode == Activity.RESULT_OK)
                    getResultFromApi()
            }
            RC_AUTH_PERMISSION -> {
                Toast.makeText(this,"구글 인증이 필요합니다.",Toast.LENGTH_SHORT).show()
                getResultFromApi()
            }

        }
    }


}




