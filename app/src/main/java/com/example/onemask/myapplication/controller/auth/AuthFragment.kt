package com.example.onemask.myapplication.controller.auth

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.onemask.myapplication.R
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_auth.*
import javax.inject.Inject


private const val REQUEST_PERMISSION_GET_ACCOUNTS = 1001
private const val REQUEST_ACCOUNT_PICKER = 1002


class AuthFragment : DaggerFragment() {

    lateinit var gso : GoogleSignInOptions

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
        button_auth.setOnClickListener { choseAccount() }
    }

    //Using EasyPermissions
//    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
//    private fun choseAccount() {
//        if (hasPermissions(requireContext(), android.Manifest.permission.GET_ACCOUNTS)) {
//            startActivityForResult(googleAccountCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
//        }else{
//            EasyPermissions.requestPermissions(
//                this,
//                "구글 계정 권한이 필요합니다.",
//                REQUEST_PERMISSION_GET_ACCOUNTS,
//                android.Manifest.permission.GET_ACCOUNTS
//            )
//        }
//    }

    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    private fun makeGoogleSignIn(){
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestEmail()
            .build()
    }

    //Delete EasyPermissions
    private fun choseAccount(){


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
