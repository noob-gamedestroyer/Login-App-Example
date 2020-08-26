package com.gamdestroyerr.loginapp

import android.app.ActionBar
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getDrawable
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.account_bottom_app_sheet.*
import kotlinx.android.synthetic.main.activity_user_account.*
import kotlinx.android.synthetic.main.custom_update_details_dialog.view.*
import kotlinx.android.synthetic.main.delete_dialog.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserAccountActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_account)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val mBottomAppSheetBehavior = BottomSheetBehavior.from(account_bottom_app_sheet)
        accountOptionBtn.setOnClickListener {
            //sets the state of bottom app sheet to expanded
            mBottomAppSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        /*  This checks for the current user in the firebase database
            and sets name and email in their respective text views
            including if the user has verified email or not. */
        currentUser?.let { user ->
            emailTxt.text = user.email
            nameTxt.text = user.displayName
            if (user.isEmailVerified) {
                emailVerified.text = getString(R.string.True)
            } else {
                emailVerified.text = getString(R.string.False)
            }
        }

        twitter.setOnClickListener {
            val uri: Uri = Uri.parse("https://twitter.com/rajatgupta35v")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        instagram.setOnClickListener {
            val uri: Uri = Uri.parse("https://www.instagram.com/rajatkumar_r0")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        github.setOnClickListener {
            val uri: Uri = Uri.parse("https://github.com/noob-gamedestroyer/Login-App-Example")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        //onClick Listener for SignOut btn
        signOutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@UserAccountActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }

        updateAccBtn.setOnClickListener {
            mBottomAppSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val view =
                LayoutInflater.from(this).inflate(R.layout.custom_update_details_dialog, null)
            val dialog = MaterialAlertDialogBuilder(this)
                .setView(view)
                .setBackground(getDrawable(this, R.drawable.rounded_dialog))
                .setCancelable(true)
                .show()

            view.updateCredentialsBtn.setOnClickListener {
                if (view.newNameTxtInputLayout.editText?.text.toString().isNotEmpty()) {
                    val updates = UserProfileChangeRequest.Builder()
                        .setDisplayName(view.newNameTxtInputLayout.editText?.text.toString())
                        .build()
                    currentUser?.updateProfile(updates)?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            val lottie = view.lottieUpdateIllustration
                            lottie.setAnimation(R.raw.done)
                            lottie.playAnimation()
                            lottie.repeatCount = LottieDrawable.RESTART
                            lottie.background = getDrawable(this, R.color.green)
                            view.updateCredentialsBtn.text = getString(R.string.dismiss)
                            view.updateCredentialsBtn.setOnClickListener {
                                dialog.dismiss()
                            }
                            currentUser.let { user ->
                                nameTxt.text = user.displayName
                            }

                        } else {
                            val lottie = view.lottieUpdateIllustration
                            lottie.setAnimation(R.raw.error)
                            lottie.playAnimation()
                            lottie.repeatCount = 0
                            lottie.background = getDrawable(this, android.R.color.black)

                            val eMessage: String by lazy {
                                it.exception?.message.toString()
                            }
                            Toast.makeText(this, eMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "No Name Entered", Toast.LENGTH_LONG).show()
                    val lottie = view.lottieUpdateIllustration
                    lottie.setAnimation(R.raw.error)
                    lottie.playAnimation()
                    lottie.repeatCount = 0
                    lottie.background = getDrawable(this, android.R.color.black)
                }
            }
        }

        deleteAccountBtn.setOnClickListener {
            val view1 = LayoutInflater.from(this).inflate(R.layout.delete_dialog, null)
            val dialog = MaterialAlertDialogBuilder(this)
                .setView(view1)
                .setBackground(getDrawable(this, R.drawable.rounded_dialog))
                .setCancelable(true)
                .show()

            var email = "test@test.com"

            view1.deleteDialogBtn.setOnClickListener {
                val password = view1.verify_password_textField.editText!!.text.toString()
                currentUser?.let {
                    email = it.email.toString()
                }
                if (password.isEmpty()){
                    view1.verify_password_textField.editText!!.error = "Password Field Empty"
                    return@setOnClickListener
                }
                val credentials = EmailAuthProvider.getCredential(email, password)
                Log.d("delete", password + email)
                    Log.d("delete", "onCreate: Re-authenticated ")
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            withContext(Dispatchers.IO) {
                                currentUser?.reauthenticate(credentials)?.await()
                                currentUser?.delete()
                            }
                            Log.d("delete", "onCreate: Re-authenticated ")

                            //this will ensure that the keyboard gets dismissed as soon as this activity is closed
                            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            inputMethodManager.hideSoftInputFromWindow(
                                view1.applicationWindowToken,
                                0
                            )

                            dialog.dismiss()
                            Toast.makeText(
                                this@UserAccountActivity,
                                "Re-authenticated and deleted successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(
                                this@UserAccountActivity,
                                LoginActivity::class.java
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            startActivity(intent)

                        } catch (e: Exception) {
                            view1.verify_password_textField.editText!!.error = e.message +
                                    " SignOut Manually or Restart the app"
                            Toast.makeText(this@UserAccountActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                }
            }

            view1.cancelDialog.setOnClickListener {
                dialog.dismiss()
            }
        }
        cancelUserAcctBtn.setOnClickListener {
            mBottomAppSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

}