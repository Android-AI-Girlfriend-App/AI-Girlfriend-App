package com.example.myaisweetheart

import android.content.Context
import android.provider.ContactsContract.Data
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

object AIManager {
    private const val url = "https://api.openai.com/v1/completions"
    private var user = ""
    private var gf = ""
    private var shy: Int = 0
    private var pessimistic: Int = 0
    private var ordinary: Int = 0
    private var hobbies = ""

    fun getResponse(
        context: Context,
        query: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val chatHistory = getChatHistoryFromDatabase(context)
        val prompt = buildPrompt(chatHistory, query)
        // creating a queue for request queue.
        val queue: RequestQueue = Volley.newRequestQueue(context)
        // creating a json object on below line.
        val jsonObject: JSONObject? = JSONObject()
        // adding params to json object.
        jsonObject?.put("model", "text-davinci-003")
        jsonObject?.put("prompt", prompt)
        jsonObject?.put("temperature", 0.7)
        jsonObject?.put("max_tokens", 1000)
        jsonObject?.put("top_p", 0.7)
        jsonObject?.put("frequency_penalty", 1.5)
        jsonObject?.put("presence_penalty", 1.0)

        // on below line making json object request.
        val postRequest: JsonObjectRequest =
            // on below line making json object request.
            object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                Response.Listener { response ->
                    // on below line getting response message and setting it to text view.
                    val responseMsg: String =
                        response.getJSONArray("choices").getJSONObject(0).getString("text")
                    onResponse(responseMsg)
                },
                // adding on error listener
                Response.ErrorListener { error ->
                    Log.e("TAGAPI", "Error is : " + error.message + "\n" + error)
                    onError(error.message ?: "Unknown error")
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    // adding headers on below line.
                    params["Content-Type"] = "application/json"
                    params["Authorization"] =
                        "Bearer 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"
                    return params;
                }
            }

        // on below line adding retry policy for our request.
        postRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        })
        // on below line adding our request to queue.
        queue.add(postRequest)
    }

    fun setusername(name: String){
        user = name
    }

    fun setGfName(name: String){
        gf = name
    }

    fun setPersonality(personality: Int, trait: Int){
        when (trait) {
            0 -> shy = personality
            1 -> pessimistic = personality
            else -> ordinary = personality
        }
    }

    fun setHobbies(hobbiesString: String) {
        hobbies = hobbiesString
    }

    fun setProfile(dbuser: String, dbgf: String, dbshy: Int, dbpessimistic: Int, dbordinary: Int, dbhobbies: String) {
        user = dbuser
        gf = dbgf
        shy = dbshy
        pessimistic = dbpessimistic
        ordinary = dbordinary
        hobbies = dbhobbies
    }

    fun getUserName(): String {
        return user
    }

    fun getGfName(): String {
        return gf
    }

    fun getShy(): Int {
        return shy
    }

    fun getPessimistic(): Int {
        return pessimistic
    }

    fun getOrdinary(): Int {
        return ordinary
    }

    fun getHobby(): String {
        return hobbies
    }

    private fun getChatHistoryFromDatabase(context: Context): String {
        val dbHelper = DatabaseHelper(context)
        val cursor = dbHelper.getLastTwoEntries()
        val chatHistory = StringBuilder()

        if (cursor != null) {
            val messageIndex = cursor.getColumnIndex("msg")
            // Check if the column exists in the cursor
            if (messageIndex != -1) {
                while (cursor.moveToNext()) {
                    val message = cursor.getString(messageIndex)
                    chatHistory.append(message).append(" ")
                }
            } else {
                Log.e("AnotherClass", "Column 'columnName' not found in the cursor")
            }
            cursor?.close()
        }
        return chatHistory.toString().trim()
    }

    private fun buildPrompt(chatHistory: String, currentQuery: String): String {
        val instructions = "Roleplay as my girlfriend. Your name is $gf. My name is $user. You're 21 years old. You are a college student. \n" +
                "You love me. You're open to any conversation. For personality traits (Don't mention these in conversation): " +
                "On a scale of 0 to 10, 0 being very shy and 10 being very flirty, you're $shy.\n" +
                "Then on a scale of 0 to 10, 0 being very pessimistic and 10 being very optimistic, you're $pessimistic.\n" +
                "Then on a scale of 0 to 10, 0 being very ordinary and 10 being very mysterious, you're $ordinary.\n" +
                "Here are my interests and hobbies: $hobbies.\n" +
                "Try to keep your responses within 50 words."
        return "$instructions \n\n$chatHistory $currentQuery"
    }
}
