package com.example.myaisweetheart

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    // creating variables on below line.
    private lateinit var responseTV: TextView
    private lateinit var questionTV: TextView
    private lateinit var queryEdt: TextInputEditText

    private val dbHelper = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initializing variables on below line.
        responseTV = findViewById(R.id.idTVResponse)
        questionTV = findViewById(R.id.idTVQuestion)
        queryEdt = findViewById(R.id.idEdtQuery)
        // adding editor action listener for edit text on below line.

        //getResponse(template)
        val cursor: Cursor? = dbHelper.getLastTwoEntries()

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    // Check if the column exists in the cursor
                    val columnIndex = it.getColumnIndex("msg")
                    val column2Index = it.getColumnIndex("sender")

                    if (columnIndex != -1 && column2Index != -1) {
                        val columnValue = it.getString(columnIndex)
                        val column2Value = it.getString(column2Index)

                        if (column2Value == "1") {
                            questionTV.text = columnValue
                        } else {
                            responseTV.text = columnValue
                        }
                    } else {
                        // Handle the case where one or both columns are not found
                        if (columnIndex == -1) {
                            Log.e("YourTag", "Column 'columnName' not found in the cursor")
                        }
                        if (column2Index == -1) {
                            Log.e("YourTag", "Column 'sender' not found in the cursor")
                        }
                        // You might throw an exception, display a user-friendly message, or take other actions
                    }
                } while (it.moveToNext())
            }
        }

        queryEdt.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // setting response tv on below line.
                responseTV.text = "..."
                // validating text
                if (queryEdt.text.toString().isNotEmpty()) {
                    // calling get response to get the response.
                    getResponse(queryEdt.text.toString())
                } else {
                    Toast.makeText(this, "Chat with your gf..", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })

        val settingsBtn: ImageButton = findViewById(R.id.settings_btn)
        settingsBtn.setOnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
    }

    private fun getResponse(query: String) {
        questionTV.text = query
        queryEdt.setText("")

        AIManager.getResponse(applicationContext, query,
            onResponse = { responseMsg ->
                questionTV.text = query
                responseTV.text = responseMsg
                dbHelper.insertData(query, true)
                dbHelper.insertData(responseMsg, false)
            }
        ) { error ->
            Log.e("TAGAPI", "Error is: $error")
        }
    }

    override fun onPause() {
        super.onPause()
        val prefs = getSharedPreferences("X", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("lastActivity", javaClass.name)
        editor.apply()
    }
}
