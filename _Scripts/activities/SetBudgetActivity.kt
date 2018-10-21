package com.mycahkrason.dailysaver

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_set_budget.*
import java.util.*

class SetBudgetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)

        //set up preferences
        val preferences = this.getSharedPreferences("SpendingData", android.content.Context.MODE_PRIVATE)

        //Retrieve the budgetSetTimes from the defaults
        var budgetSetTimes = preferences.getInt("BudgetSetTimes", 0)

        cancelBtn.setOnClickListener {
            super.finish()
        }

        updateBudgetBtn.setOnClickListener {

            //save data to
            val amountOfMoney = moneyToSpendInput.text.toString()
            val numberOfDays = numberOfDaysInput.text.toString()

            //check input fields
            if(amountOfMoney.isEmpty() || numberOfDays.isEmpty()){
                //Create alert
                val builder = AlertDialog.Builder(this@SetBudgetActivity)
                // Display a message on alert dialog
                builder.setMessage("All fields must be filled in.")
                // Display a neutral button on alert dialog
                builder.setNeutralButton("Dismiss") { _, _ ->
                    //Do nothing
                }
                // Finally, make the alert dialog using builder
                val dialog: AlertDialog = builder.create()
                // Display the alert dialog on app interface
                dialog.show()

            }else{

                val lastSavedDay = numberOfDays.toDouble()
                val editor = preferences.edit()

                editor.putString("NumberOfDays", numberOfDays)
                editor.putString("LastSavedDay", lastSavedDay.toString())
                editor.putString("AmountOfMoney", amountOfMoney)

                val todaysBudget = (amountOfMoney.toDouble() / numberOfDays.toDouble())
                editor.putString("TodaysBudget", todaysBudget.toString())

                //Set the time count down
                val startDate = SystemClock.elapsedRealtime()
                editor.putString("StartDate", startDate.toString())

                Log.d("findIt", "$startDate")

                //update the budgetSetTimes
                budgetSetTimes += 1
                editor.putInt("BudgetSetTimes", budgetSetTimes)
                Log.d("findIt", "Budget Set Times has increased = $budgetSetTimes")

                editor.apply()

                super.finish()
            }
        }

        numberOfDaysInput.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyBoard(v)
            }
        }

        moneyToSpendInput.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyBoard(v)
            }
        }
    }

    //hide the keyboard
    fun hideKeyBoard(v : View){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}
