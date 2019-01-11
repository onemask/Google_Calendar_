package com.example.onemask.myapplication


import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.onemask.myapplication.repository.CalendarRepository
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import dagger.android.support.DaggerAppCompatActivity

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.hasPermissions
import javax.inject.Inject

private const val REQUEST_PERMISSION_GET_ACCOUNTS = 1001
private const val REQUEST_CODE_PLAY_SERVICE = 1002
private const val REQUEST_ACCOUNT_PICKER = 1003
private const val REQUEST_AUTHORIZATION = 1004
private const val RC_AUTH_PERMISSION = 1005


class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var  googleAccountCredential: GoogleAccountCredential

    @Inject
    lateinit var googleCalendarRepository: CalendarRepository

    private lateinit var compositeDisposable: CompositeDisposable

    private var REQUEST_ACCOUNT: String = "accountName"
    private var calendarId: String = "onemask14@gmail.com"
    private var isgoogle: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        compositeDisposable = CompositeDisposable()
        button_auth.setOnClickListener {
            getAuth()
        }

    }

    fun getAuth() {

        isGooglePlayServiceAvailable()

        button_holiday.setOnClickListener {
            getCalendarList()
        }

        button_month.setOnClickListener {
            getEventList(calendarId)
        }

    }

    private fun getCalendarList() {
        googleCalendarRepository.getCalendarList()
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.items }
            .subscribe({
                it.forEach { item ->
                    val button = Button(this)
                    button.text = item.summary
                    button.setOnClickListener {
                        getEventList(item.id)
                    }
                    layout_button.addView(button)
                    //scrollView2.addView(button)
                    Log.d("지금 나오는 item.id", item.id)
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

    private fun getResultFromApi() {
        if (!isGooglePlayServiceAvailable()) {
            acquireGooglePlayServices()
        } else {
            googleAccountCredential.selectedAccountName?.let {
                //getCalendarList()
                REQUEST_ACCOUNT = googleAccountCredential.selectedAccountName
            }.let {
                //choseAccount()
            }
        }
    }

    private fun isGooglePlayServiceAvailable(): Boolean {
        googleAccountCredential.selectedAccountName?.let {
            Toast.makeText(this, "구글 인증이 되었습니다.", Toast.LENGTH_SHORT).show()
            isgoogle = true
            changeButton()
            return true
        }.let {
            Toast.makeText(this, "구글 인증을 선택해주세요.", Toast.LENGTH_SHORT).show()
            isgoogle = false
            choseAccount()
            return false
        }
    }

    private fun changeButton(){
        button_auth.visibility = View.INVISIBLE
        button_month.visibility = View.VISIBLE
        button_holiday.visibility = View.VISIBLE
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
        when (requestCode) {
            REQUEST_CODE_PLAY_SERVICE -> {
                if (resultCode == Activity.RESULT_OK) {
                    text_field.text = "구글 플레이 서비스를 설치 해주세요."
                } else
                    getResultFromApi()
            }
            REQUEST_ACCOUNT_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val accountName: String? = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    accountName?.let {
                        getPreferences(Context.MODE_PRIVATE).edit().apply {
                            putString(REQUEST_ACCOUNT, accountName)
                            getResultFromApi()
                            apply()
                        }
                        googleAccountCredential.selectedAccountName = accountName
                    }
                }
            }
            REQUEST_AUTHORIZATION -> {
                if (requestCode == Activity.RESULT_OK)
                    getResultFromApi()
            }
            RC_AUTH_PERMISSION -> {
                Toast.makeText(this, "구글 인증이 필요합니다.", Toast.LENGTH_SHORT).show()
                getResultFromApi()
            }

        }
    }
}



