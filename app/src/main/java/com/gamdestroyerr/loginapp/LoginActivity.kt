package com.gamdestroyerr.loginapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.custom_register_dialog_container.view.*
import kotlinx.android.synthetic.main.custom_reset_password_dialog.view.*
import kotlinx.android.synthetic.main.register_bottom_sheet.*
import kotlinx.android.synthetic.main.register_bottom_sheet.cancelBtn
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var check: Boolean= false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        var newPass:String?
        var conPass:String?

        val mBottomSheetBehavior = BottomSheetBehavior.from(registerBottomSheet)
        passwordTxtInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        setPasswordTxtInput.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        confirmPasswordTxtInput.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

        /* this statement below hides the signIn and register button to show
           above the bottom sheet */
        bottomSheetContainer.elevation = 6f

        /* To inflate custom dialog layout file to inflate in a view and
         then the view is passed to the dialog builder */
        val factory = LayoutInflater.from(this)
        val view1 :View = factory.inflate(R.layout.custom_register_dialog_container, null)

        //----------------------------------------onClick Listeners------------------------------------------------------------------Start-------
        registerBtn.setOnClickListener {

            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            mBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback(){

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    cancelBtn.rotation = (slideOffset * 135)
                }
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                }
            })
        }

        signUpBtn.setOnClickListener {
            newPass = setPasswordTxtInput.editText?.text.toString()
            conPass = confirmPasswordTxtInput.editText?.text.toString()

            if (newPass.isNullOrBlank() || emailTxtRegister.editText?.text.toString().isBlank()) {
                passwordMatchConfirmation.text = getString(R.string.not_empty_password)
                passwordMatchConfirmation.visibility = View.VISIBLE
            } else if (newPass == conPass) {
                registerUser(newPass!!,view1,mBottomSheetBehavior)
                passwordMatchConfirmationMain.visibility = View.INVISIBLE
            } else if (newPass != conPass){
                passwordMatchConfirmation.text = getString(R.string.password_mismatch)
                passwordMatchConfirmation.visibility = View.VISIBLE
            } else{
                passwordMatchConfirmation.visibility = View.INVISIBLE
            }
        }

        signInBtn.setOnClickListener {
            val email: String? = emailTxtInputLayout.editText?.text.toString()
            val password:String? = passwordTxtInputLayout.editText?.text.toString()

            if (email.isNullOrBlank() || password.isNullOrBlank()) {
                passwordMatchConfirmationMain.text = getString(R.string.not_empty_password)
                passwordMatchConfirmationMain.visibility = View.VISIBLE
            } else {
                loginUser(email, password)
                passwordMatchConfirmationMain.visibility = View.INVISIBLE
            }
        }

        forgotBtn.setOnClickListener {

            val view: View = LayoutInflater.from(this).inflate(R.layout.custom_reset_password_dialog, null)
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
                if (email.isNotEmpty()){
                    resetPassword(email,view)
                } else {
                    Toast.makeText(this, "You must enter an email id to send password reset link", Toast.LENGTH_LONG).show()
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
        password:String,
        view1: View,
        mBottomSheetBehavior:BottomSheetBehavior<ConstraintLayout>) {

        val email = emailTxtRegister.editText?.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    withContext(Dispatchers.IO) {
                        auth.createUserWithEmailAndPassword(email, password).await()
                    }
                    check = true
                    if (check) {
                        showDialog(view1, mBottomSheetBehavior)
                    }
                    Toast.makeText(this@LoginActivity,
                        "Registered",
                        Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    passwordMatchConfirmation.text = e.message
                    passwordMatchConfirmation.visibility = View.VISIBLE

                }
            }

        }
    }

    //Firebase login function to signIn existing user
    private fun loginUser(email: String, password:String) {

        if (email.isNotEmpty() && password.isNotEmpty()) {

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    withContext(Dispatchers.IO) {
                        auth.signInWithEmailAndPassword(email, password).await()
                    }
//                    check = true
//                    if (check) {
//
//                    }
                    passwordMatchConfirmationMain.visibility = View.INVISIBLE
                    Toast.makeText(this@LoginActivity,
                        "Logged In",
                        Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    passwordMatchConfirmationMain.text = e.message
                    passwordMatchConfirmationMain.visibility = View.VISIBLE

                    Toast.makeText(this@LoginActivity,
                        e.message,
                        Toast.LENGTH_SHORT).show()
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
                    val lottie = view.lottieConfirmIllustration
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

    private fun checkLoggedInState() {
        if(auth.currentUser == null) {
            Toast.makeText(this, "Not Logged In", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show()
        }

    }

    private fun showDialog(view1: View, mBottomSheetBehavior:BottomSheetBehavior<ConstraintLayout>){
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

    override fun onStart() {
        super.onStart()
        checkLoggedInState()
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