package com.example.onemask.myapplication.controller.auth

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.onemask.myapplication.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_auth.*
import timber.log.Timber
import java.util.jar.Manifest
import javax.inject.Inject


private const val REQUEST_ACCOUNT_PICKER = 1002
private const val RP_GET_ACCOUNTS = 1003

class AuthFragment : DaggerFragment() {

    lateinit var gso: GoogleSignInOptions
    lateinit var mGoogleSignInClient: GoogleSignInClient

    @Inject
    lateinit var googleAccountCredential: GoogleAccountCredential

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        makeGoogleSignIn()

        //Delete Easypermission
        button_auth.setOnClickListener {
            selectAccount()
        }

    }

    private fun selectAccount() {
        val permission = android.Manifest.permission.GET_ACCOUNTS
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //현재 계정 상태 보여주는 애.
            startActivityForResult(googleAccountCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
        } else
            requestPermissions(arrayOf(permission), RP_GET_ACCOUNTS)
    }


    private fun makeGoogleSignIn() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this.context!!.applicationContext, gso)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            RP_GET_ACCOUNTS->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startActivityForResult(googleAccountCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
                else
                    Toast.makeText(context,"구글 계정 권한 접근이 필요합니다.",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ACCOUNT_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val accountName: String? = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    accountName?.let {
                        googleAccountCredential.selectedAccountName = it
                        movetoCalendarListFragment()
                    } ?: run {
                        Toast.makeText(requireContext(), "구글 인증이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun movetoCalendarListFragment() {
        AuthFragmentDirections.actionDestAuthToDestCalendarSelect().apply {
            findNavController().navigate(this)
        }
    }

}
