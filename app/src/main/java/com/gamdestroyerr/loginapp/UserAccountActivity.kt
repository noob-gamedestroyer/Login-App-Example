package com.gamdestroyerr.loginapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.account_bottom_app_sheet.*
import kotlinx.android.synthetic.main.activity_user_account.*

class UserAccountActivity : AppCompatActivity() {

    private var currentUser =  FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_account)
        //this is the activity that will show all the user details

        val mBottomAppSheetBehavior = BottomSheetBehavior.from(account_bottom_app_sheet)
        accountOptionBtn.setOnClickListener {
            mBottomAppSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        currentUser?.let { user ->
            emailTxt.text = user.email
            nameTxt.text = user.displayName
            if (user.isEmailVerified) {
                emailVerified.text = getString(R.string.True)
            } else {
                emailVerified.text = getString(R.string.False)
            }
        }

        signOutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@UserAccountActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }
}