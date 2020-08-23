package com.gamdestroyerr.loginapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

class SplashActivity :AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Main", "onCreate: SplashActivity Started ")

        //Since checking for current user is a network call we use IO dispatchers of CoroutineScope
        CoroutineScope(Dispatchers.IO).launch {
                checkLoggedInState()
                delay(80)
        }

    }

    //this is a suspend function to check for the current user
    //If the user is signed in this directly opens userAccountActivity
    //If not login activity is opened
    private fun checkLoggedInState(){

        if (FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(this,UserAccountActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}