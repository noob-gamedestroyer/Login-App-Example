package com.gamdestroyerr.loginapp

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_dialog_container.view.*
import kotlinx.android.synthetic.main.register_bottom_sheet.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mBottomSheetBehavior = BottomSheetBehavior.from(registerBottomSheet)

        passwordTxtInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        setPasswordTxtInput.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        confirmPasswordTxtInput.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

        //this statement below hides the signIn and register button to show above the bottom sheet
        bottomSheetContainer.elevation = 6f

        val factory = LayoutInflater.from(this)
        val view1 :View = factory.inflate(R.layout.custom_dialog_container, null)

        registerBtn.setOnClickListener {

            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            mBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback(){

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    cancelBtn.rotation = (slideOffset * 135)
                }

                @SuppressLint("UseCompatLoadingForDrawables")
                override fun onStateChanged(bottomSheet: View, newState: Int) {

                    signUpBtn.setOnClickListener {
                        signUpBtn.visibility = View.INVISIBLE

                        val dialog = MaterialAlertDialogBuilder(this@MainActivity)
                            .setView(view1)
                            .setBackground(getDrawable(R.drawable.rounded_dialog))
                            .setCancelable(false)
                            .show()

                        view1.dialogBtn.setOnClickListener {
                            dialog.dismiss()
                            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            signUpBtn.visibility = View.VISIBLE
                        }
                    }

                }
            })
        }

        cancelBtn.setOnClickListener {
            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}