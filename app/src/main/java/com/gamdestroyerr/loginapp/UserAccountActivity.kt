package com.gamdestroyerr.loginapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getDrawable
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
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
            mBottomAppSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    cancelUserAcctBtn.rotation = (slideOffset * 360)
                }

            })
        }

        val layoutParams = profilePic.layoutParams
        val widDpi = resources.displayMetrics.densityDpi
        Log.d("TAG", widDpi.toString())

        //since I did not make different layout files for different screen sizes
        //Layout adjustment is done programmatically, this method is however not recommended
        when {
            widDpi >= 520 -> {
                layoutParams.height = 100.toDp(this)
                profilePic.layoutParams = layoutParams
                Log.d("TAG", "xxxHigh")
            }
            widDpi in 480..519 -> {
                layoutParams.height = 110.toDp(this)
                profilePic.layoutParams = layoutParams
                Log.d("TAG", "xxHigh")
            }
            widDpi in 410..479 -> {
                layoutParams.height = 125.toDp(this)
                profilePic.layoutParams = layoutParams
                Log.d("TAG", "xHigh")
            }
            widDpi in 370..409 -> {
                layoutParams.height = 180.toDp(this)
                profilePic.layoutParams = layoutParams
                Log.d("TAG", "High")
            }
            widDpi in 350..369 -> {
                layoutParams.height = 190.toDp(this)
                profilePic.layoutParams = layoutParams
                Log.d("TAG", "medium")
            }
            widDpi in 300..349 -> {
                layoutParams.height = 195.toDp(this)
                profilePic.layoutParams = layoutParams
                Log.d("TAG", "midMedium")
            }
            widDpi in 260..299 -> {
                layoutParams.height = 200.toDp(this)
                profilePic.layoutParams = layoutParams
                Log.d("TAG", "low")
            }
            widDpi < 259 -> {
                layoutParams.height = 210.toDp(this)
                profilePic.layoutParams = layoutParams
                Log.wtf("TAG", "superLow")
            }

        }

        /*  This checks for the current user in the firebase database
            and sets name and email in their respective text views
            including if the user has verified email or not. */
        currentUser?.let { user ->
            emailTxt.text = user.email
            nameTxt.text = user.displayName
            if (user.isEmailVerified) {
                emailVerified.text = getString(R.string.verified)
            } else {
                emailVerified.text = getString(R.string.not_verified)
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
            val uri: Uri =
                Uri.parse("https://github.com/noob-gamedestroyer/Login-App-Example/releases")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        emailVerified.setOnClickListener {
            if (currentUser!!.isEmailVerified) {
                Snackbar.make(it, "Email Already Verified", Snackbar.LENGTH_LONG)
                    .show()
            } else {
                currentUser.sendEmailVerification().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Snackbar.make(it, "Email Verification Link Sent", Snackbar.LENGTH_LONG)
                            .show()
                    } else {
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }

        //onClick Listener for SignOut btn
        signOutBtn.setOnClickListener {
            try {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Signed Out", Toast.LENGTH_LONG).show()
                val intent = Intent(this@UserAccountActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }

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

            view1.verify_password_textField.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

            view1.verify_password_textField.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    view1.verify_password_textField.isEndIconVisible = true
                }

                override fun afterTextChanged(p0: Editable?) {
                    view1.verify_password_textField.isEndIconVisible = true
                }
            })
            view1.deleteDialogBtn.setOnClickListener {
                var email = String()
                val password = view1.verify_password_textField.editText!!.text.toString()
                currentUser?.let {
                    email = it.email.toString()
                }
                if (password.isEmpty()) {
                    view1.verify_password_textField.isEndIconVisible = false
                    view1.verify_password_textField.editText!!.error = "Password Field Empty"
                    return@setOnClickListener
                }
                val credentials = EmailAuthProvider.getCredential(email, password)
                Log.d("delete", password + email)
                Log.d("delete", "onCreate: Re-authenticated")
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        withContext(Dispatchers.IO) {
                            currentUser?.reauthenticate(credentials)?.await()
                            currentUser?.delete()
                        }
                        Log.d("delete", "onCreate: Re-authenticated")

                        //this will ensure that the keyboard gets dismissed as soon as this activity is closed
                        val inputMethodManager =
                            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
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
                        view1.verify_password_textField.isEndIconVisible = false
                        view1.verify_password_textField.editText!!.error = e.message
                        Toast.makeText(this@UserAccountActivity, e.message, Toast.LENGTH_SHORT)
                            .show()

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