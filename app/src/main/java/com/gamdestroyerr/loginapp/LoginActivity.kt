package com.gamdestroyerr.loginapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.custom_register_dialog_container.view.*
import kotlinx.android.synthetic.main.custom_reset_password_dialog.view.*
import kotlinx.android.synthetic.main.register_bottom_sheet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_login)
        Log.d("Main", "onCreate: thread login activity ")

        val layoutParams = signInImage.layoutParams
        val widDpi = resources.displayMetrics.densityDpi
        Log.d("TAG", widDpi.toString())

        //since I did not make different layout files for different screen sizes
        //Layout adjustment is done programmatically, this method is however not recommended
        when {
            widDpi >= 520 -> {
                layoutParams.height = 151.toDp(this)
                signInImage.layoutParams = layoutParams
                Log.d("TAG", "xxHigh")
            }
            widDpi in 410..519 -> {
                layoutParams.height = 161.toDp(this)
                signInImage.layoutParams = layoutParams
                Log.d("TAG", "xHigh")
            }
            widDpi in 370..409 -> {
                layoutParams.height = 181.toDp(this)
                signInImage.layoutParams = layoutParams
                Log.d("TAG", "High")
            }
            widDpi in 300..369 -> {
                layoutParams.height = 187.toDp(this)
                signInImage.layoutParams = layoutParams
                Log.d("TAG", "medium")
            }
            widDpi in 260..299 -> {
                layoutParams.height = 222.toDp(this)
                signInImage.layoutParams = layoutParams
                Log.d("TAG", "low")
            }
            widDpi < 259 -> {
                layoutParams.height = 233.toDp(this)
                signInImage.layoutParams = layoutParams
                Log.d("TAG", "superLow")
            }

        }

        var newPass: String?
        var conPass: String?

        val mBottomSheetBehavior = BottomSheetBehavior.from(registerBottomSheet)
        passwordTxtInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        setPasswordTxtInput.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        confirmPasswordTxtInput.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

        //----------------------------------------onClick Listeners------------------------------------------------------------------Start-------
        registerBtn.setOnClickListener {

            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            mBottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    cancelBtn.rotation = (slideOffset * 360)
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        nameTxt.editText?.text?.clear()
                        emailTxtRegister.editText?.text?.clear()
                        setPasswordTxtInput.editText?.text?.clear()
                        confirmPasswordTxtInput.editText?.text?.clear()
                    }

                }
            })
        }

        signUpBtn.setOnClickListener {
            newPass = setPasswordTxtInput.editText?.text.toString()
            conPass = confirmPasswordTxtInput.editText?.text.toString()

            when {
                newPass.isNullOrBlank() || emailTxtRegister.editText?.text.toString()
                    .isBlank() || nameTxt.editText?.text.toString().isEmpty() -> {
                    Toast.makeText(this, getString(R.string.not_empty_naEmPass), Toast.LENGTH_SHORT)
                        .show()
                }
                newPass == conPass -> {
                    registerUser(newPass!!, mBottomSheetBehavior)
                }
                newPass != conPass -> {
                    Toast.makeText(this, getString(R.string.password_mismatch), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        signInBtn.setOnClickListener {
            val email: String? = emailTxtInputLayout.editText?.text.toString()
            val password: String? = passwordTxtInputLayout.editText?.text.toString()

            if (email.isNullOrBlank() || password.isNullOrBlank()) {
                Snackbar.make(it, getString(R.string.not_empty_emPass), Snackbar.LENGTH_LONG).show()
            } else {
                loginUser(email, password)
            }
        }

        forgotBtn.setOnClickListener {
            val view: View =
                LayoutInflater.from(this).inflate(R.layout.custom_reset_password_dialog, null)
            val dialog = MaterialAlertDialogBuilder(this)
                .setView(view)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_dialog))
                .setCancelable(true)
                .show()
            view.cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

            view.resetBtn.setOnClickListener {
                val email = view.resetEmailTxtInputLayout.editText?.text.toString()
                if (email.isNotEmpty()) {
                    resetPassword(email, view)
                } else {
                    Toast.makeText(
                        this,
                        "You must enter an email id to send password reset link",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }

        //onClick Listener for circular cancel button of bottom app sheet
        cancelBtn.setOnClickListener {
            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        //-------------------------------------------------------------onClickListener----------------------------End----------
    }

    //Firebase register function to register new user
    private fun registerUser(
        password: String,
        mBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    ) {

        val email = emailTxtRegister.editText?.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    withContext(Dispatchers.IO) {
                        auth.createUserWithEmailAndPassword(email, password).await()
                    }
                    val updates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nameTxt.editText?.text.toString())
                        .build()
                    auth.currentUser?.updateProfile(updates)
                        /* To inflate custom dialog layout file to inflate in a view and
                        then the view is passed to the dialog builder */
                    auth.signOut()
                    showDialog(mBottomSheetBehavior)

                    Toast.makeText(
                        this@LoginActivity,
                        "Registered",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_SHORT).show()

                }
            }

        }
    }

    //Firebase login function to signIn existing user
    private fun loginUser(email: String, password: String) {

        if (email.isNotEmpty() && password.isNotEmpty()) {

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    withContext(Dispatchers.IO) {
                        auth.signInWithEmailAndPassword(email, password).await()
                    }
                    Toast.makeText(this@LoginActivity, "Logged In", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, UserAccountActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    private fun resetPassword(email: String, view: View) {
        if (email.isNotEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    withContext(Dispatchers.IO) {
                        auth.sendPasswordResetEmail(email).await()
                    }
                    val lottie = view.lottieUpdateIllustration
                    lottie.setAnimation(R.raw.send_email)
                    lottie.setBackgroundColor(getColor(R.color.newBackground))
                    lottie.speed = 2f
                    lottie.repeatCount = 0
                    lottie.playAnimation()
                    view.resetBtn.text = getString(R.string.sent)
                    view.cancelBtn.text = getString(R.string.done)

                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun showDialog(
        mBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    ) {
        val factory = LayoutInflater.from(this@LoginActivity)
        val view1: View = factory.inflate(R.layout.custom_register_dialog_container, null)
        signUpBtn.visibility = View.INVISIBLE
        val dialog = MaterialAlertDialogBuilder(this@LoginActivity)
            //inflated view is passed in setView()
            .setView(view1)
            .setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_dialog))
            .setCancelable(false)
            .show()

        //onClick Listener for the button in custom dialog view
        view1.dialogBtn.setOnClickListener {
            dialog.dismiss()
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            signUpBtn.visibility = View.VISIBLE
        }
    }


    //this method enables us to dismiss the keyboard if any space other than editText is touched
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}