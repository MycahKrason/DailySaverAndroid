package com.mycahkrason.dailysaver

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.ads.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_set_budget.*
import java.util.prefs.Preferences
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    //Reference the defaults
    lateinit var daysFromDefault : String
    lateinit var lastSavedDay : String
    lateinit var amountOfMoneyFromDefaults : String
    lateinit var dateFromDefaults : String
    lateinit var todaysBudget : String

    lateinit var preferences : SharedPreferences

    //Banner and Interstitial ads
    lateinit var mAdView : AdView
    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this, "APP ID")

        //Banner Ad
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        //Interstitial Ad
        mInterstitialAd = InterstitialAd(this)

        mInterstitialAd.adUnitId = "Unit ID"
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }

        mInterstitialAd.loadAd(AdRequest.Builder().build())

        preferences = this.getSharedPreferences("SpendingData", android.content.Context.MODE_PRIVATE)

        //Get all of the defaults
        daysFromDefault = preferences.getString("NumberOfDays", "")
        lastSavedDay = preferences.getString("LastSavedDay", "")
        amountOfMoneyFromDefaults = preferences.getString("AmountOfMoney", "")
        todaysBudget = preferences.getString("TodaysBudget", "")
        dateFromDefaults = preferences.getString("StartDate", "")

        daysRemaining()

        Log.d("findIt", "\n$daysFromDefault \n $lastSavedDay \n $amountOfMoneyFromDefaults \n $todaysBudget")


        newBudgetBtn.setOnClickListener {

            //Launch Set Budget activity
            val intent = Intent(this, SetBudgetActivity::class.java)
            startActivity(intent)

        }

        loginInfoBtn.setOnClickListener {

            //Create alert to tell user to enter email and password
            val builder = AlertDialog.Builder(this@MainActivity)
            // Display a message on alert dialog
            builder.setMessage("Privacy Policy and Terms of Service")
            // Display a neutral button on alert dialog
            builder.setNeutralButton("Dismiss") { _, _ ->
                //Do nothing
            }
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()

        }

        spendMoneyBtn.setOnClickListener {

            //get money from input
            if(spendMoneyInput.text.isEmpty() || spendMoneyInput.text.toString() == "."){
                //Alert user that nothing was added
                //"You must enter a valid number."
                //Create alert
                val builder = AlertDialog.Builder(this@MainActivity)
                // Display a message on alert dialog
                builder.setMessage("You must enter a valid number.")
                // Display a neutral button on alert dialog
                builder.setNeutralButton("Dismiss") { _, _ ->
                    //Do nothing
                }
                // Finally, make the alert dialog using builder
                val dialog: AlertDialog = builder.create()
                // Display the alert dialog on app interface
                dialog.show()
            }else{

                todaysBudget = preferences.getString("TodaysBudget", "")

                val subtractMoneyText = spendMoneyInput.text.toString()
                val subtractMoney = subtractMoneyText.toDouble()

                var amountOfMoneyFromDefaultsDouble = amountOfMoneyFromDefaults.toDouble()
                amountOfMoneyFromDefaultsDouble -= subtractMoney

                //Show that money is being subtracted from the daily amount
                var todaysBudgetdouble = todaysBudget.toDouble()
                todaysBudgetdouble -= subtractMoney


                val editor = preferences.edit()
                editor.putString("TodaysBudget", todaysBudgetdouble.toString())

                //Update the money in user defaults
                editor.putString("AmountOfMoney", amountOfMoneyFromDefaultsDouble.toString())

                //format probably
                todaysBudgetDisplay.text = "$%.2f".format(todaysBudgetdouble)

                spendMoneyInput.text.clear()

                editor.apply()

            }

        }

        spendMoneyInput.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyBoard(v)
            }
        }

    }

    override fun onRestart() {
        super.onRestart()

        //Get all of the defaults
        daysFromDefault = preferences.getString("NumberOfDays", "")
        lastSavedDay = preferences.getString("LastSavedDay", "")
        amountOfMoneyFromDefaults = preferences.getString("AmountOfMoney", "")
        todaysBudget = preferences.getString("TodaysBudget", "")
        dateFromDefaults = preferences.getString("StartDate", "")

        daysRemaining()
    }

    fun daysRemaining(){

        //check how many times the user has set the budget
        var budgetSetTimes = preferences.getInt("BudgetSetTimes", 0)

        if (budgetSetTimes >= 2){
            budgetSetTimes = 0

            val editor = preferences.edit()
            editor.putInt("BudgetSetTimes", budgetSetTimes)
            editor.apply()
            print("TIMES!!!! $budgetSetTimes")

            //Show the ad
            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            } else {
                Log.d("findIt", "The interstitial wasn't loaded yet.")
            }
        }

        if(dateFromDefaults.isEmpty()){

            daysRemainingDisplay.text = "0"
            todaysBudgetDisplay.text = "$0.00"

        }else{

            val startDate = dateFromDefaults.toDouble()
            val endDate = SystemClock.elapsedRealtime()

            //convert the number of days
            val elapsedTime = endDate - startDate
            val seconds = elapsedTime / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val elapsedDays = hours / 24
            val flooredDays = Math.floor(elapsedDays)
            Log.d("findIt", "$elapsedDays days")

            //change the below to elapsed Days
            var daysFromDefaultDouble = daysFromDefault.toDouble()
            daysFromDefaultDouble -= flooredDays

            if (daysFromDefaultDouble < lastSavedDay.toDouble() && daysFromDefaultDouble > 0){
                var lastSavedDayDouble = lastSavedDay.toDouble()
                lastSavedDayDouble = daysFromDefaultDouble

                val editor = preferences.edit()

                editor.putString("LastSavedDay", lastSavedDayDouble.toString())

                //update the daily budget to reflect the average of daysfromdefaults and amount of money from defaults
                todaysBudget = (amountOfMoneyFromDefaults.toDouble() / daysFromDefaultDouble).toString()
                editor.putString("TodaysBudget", todaysBudget)

                editor.apply()
            }

            if (daysFromDefaultDouble <= 0){
                daysRemainingDisplay.text = "No More days"

            }else{

                //Maybe format, round up to whole number
                "$%.2f".format(daysFromDefaultDouble)
                daysRemainingDisplay.text = "%.0f".format(daysFromDefaultDouble)

            }

            //maybe format
            if(daysFromDefaultDouble <= 0){
                todaysBudgetDisplay.text = "$%.2f".format(amountOfMoneyFromDefaults.toDouble())
            }else{
                todaysBudgetDisplay.text = "$%.2f".format(todaysBudget.toDouble())
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
