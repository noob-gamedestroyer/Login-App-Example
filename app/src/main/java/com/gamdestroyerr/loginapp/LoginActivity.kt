package com.gamdestroyerr.loginapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
                layoutParams.height = 171.toDp(this)
                signInImage.layoutParams = layoutParams
                Log.d("TAG", "xHigh")
            }
            widDpi in 370..409 -> {
                layoutParams.height = 181.toDp(this)
                signInImage.layoutParams = layoutParams
                Log.d("TAG", "High")
            }
            widDpi in 300..369 -> {
                layoutParams.height = 184.toDp(this)
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
        passwordTxtInputLayout.isEndIconVisible = false
        setPasswordTxtInput.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        confirmPasswordTxtInput.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

        if (passwordTxtInputLayout.isFocused) {
            passwordTxtInputLayout.isEndIconVisible = true
        }

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
            val nameTxtString = nameTxt.editText?.text.toString()
            val emailTxt = emailTxtRegister.editText?.text.toString()
            endIconVisibility(emailTxtRegister)
            endIconVisibility(setPasswordTxtInput)
            endIconVisibility(confirmPasswordTxtInput)
            endIconVisibility(nameTxt)

            if (emailTxt.isEmpty() && newPass.isNullOrEmpty() && conPass.isNullOrEmpty() && nameTxtString.isEmpty()) {
                setPasswordTxtInput.isEndIconVisible = false
                confirmPasswordTxtInput.isEndIconVisible = false
                nameTxt.editText?.error = getString(R.string.not_empty)
                emailTxtRegister.editText?.error = getString(R.string.not_empty)
                setPasswordTxtInput.editText?.error = getString(R.string.not_empty)
                confirmPasswordTxtInput.editText?.error = getString(R.string.not_empty)
            } else if (emailTxt.isEmpty() && newPass.isNullOrEmpty() && nameTxtString.isEmpty()) {
                setPasswordTxtInput.isEndIconVisible = false
                nameTxt.editText?.error = getString(R.string.not_empty)
                emailTxtRegister.editText?.error = getString(R.string.not_empty)
                setPasswordTxtInput.editText?.error = getString(R.string.not_empty)
            } else if (emailTxt.isEmpty() && nameTxtString.isEmpty()) {
                nameTxt.editText?.error = getString(R.string.not_empty)
                emailTxtRegister.editText?.error = getString(R.string.not_empty)
            } else {
                when {
                    emailTxt.isEmpty() -> {
                        emailTxtRegister.isEndIconVisible = false
                        emailTxtRegister.editText?.error = getString(R.string.not_empty)
                    }
                    newPass.isNullOrEmpty() -> {
                        setPasswordTxtInput.isEndIconVisible = false
                        setPasswordTxtInput.editText?.error = getString(R.string.not_empty)
                    }
                    conPass.isNullOrEmpty() -> {
                        confirmPasswordTxtInput.isEndIconVisible = false
                        confirmPasswordTxtInput.editText?.error = getString(R.string.not_empty)
                    }
                    nameTxtString.isEmpty() -> {
                        nameTxt.editText?.error = getString(R.string.not_empty)
                    }
                    newPass == conPass -> {
                        registerUser(newPass!!, mBottomSheetBehavior)
                    }
                    newPass != conPass -> {
                        confirmPasswordTxtInput.isEndIconVisible = false
                        confirmPasswordTxtInput.editText?.error =
                            getString(R.string.password_mismatch)
                    }
                }
            }
        }

        signInBtn.setOnClickListener {
            val email = emailTxtInputLayout.editText?.text.toString()
            val password = passwordTxtInputLayout.editText?.text.toString()
            endIconVisibility(passwordTxtInputLayout)
            endIconVisibility(emailTxtInputLayout)

            when {
                email.isBlank() && password.isBlank() -> {
                    emailTxtInputLayout.isEndIconVisible = false
                    passwordTxtInputLayout.isEndIconVisible = false
                    emailTxtInputLayout.editText?.error = getString(R.string.not_empty)
                    passwordTxtInputLayout.editText?.error = getString(R.string.not_empty)
                }
                else -> {
                    when {
                        email.isEmpty() -> {
//                            emailTxtInputLayout.isEndIconVisible = false
                            emailTxtInputLayout.editText?.error = getString(R.string.not_empty)
                        }
                        password.isEmpty() -> {
                            passwordTxtInputLayout.isEndIconVisible = false
                            passwordTxtInputLayout.editText?.error = getString(R.string.not_empty)
                        }
                        else -> loginUser(email, password)
                    }
                }
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
                when {
                    email.isNotEmpty() -> {
                        resetPassword(email, view)
                    }
                    else -> Toast.makeText(
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

    private fun endIconVisibility(textLayout: TextInputLayout) {
        textLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textLayout.isEndIconVisible = true
            }

            override fun afterTextChanged(p0: Editable?) {
                textLayout.isEndIconVisible = true
            }

        })
    }

//    //this method enables us to dismiss the keyboard if any space other than editText is touched
//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        if (currentFocus != null) {
//            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
//        }
//        return super.dispatchTouchEvent(ev)
//    }


}