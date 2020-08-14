package com.gamdestroyerr.loginapp

import android.content.Context
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES.M

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_dialog_container.view.*
import kotlinx.android.synthetic.main.register_bottom_sheet.*
import kotlinx.android.synthetic.main.register_bottom_sheet.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    private var check: Boolean= false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        val view1 :View = factory.inflate(R.layout.custom_dialog_container, null)

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

            if (newPass.isNullOrBlank() || emailTxtInputLayout.editText?.text.toString().isBlank()) {
                passwordMatchConfirmation.text = getString(R.string.not_empty_password)
                passwordMatchConfirmation.visibility = View.VISIBLE
            } else if (newPass == conPass) {
                registerUser(newPass!!,view1,mBottomSheetBehavior)
            } else {
                passwordMatchConfirmation.visibility = View.VISIBLE
            }

        }
        //onClick Listener for circular cancel button
        cancelBtn.setOnClickListener {
            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
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
                    Toast.makeText(this@MainActivity,
                        "Registered",
                        Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    passwordMatchConfirmation.text = e.message

                    Toast.makeText(this@MainActivity,
                        e.message,
                        Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    fun showDialog(view1: View, mBottomSheetBehavior:BottomSheetBehavior<ConstraintLayout>){
        signUpBtn.visibility = View.INVISIBLE
        val dialog = MaterialAlertDialogBuilder(this@MainActivity)
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