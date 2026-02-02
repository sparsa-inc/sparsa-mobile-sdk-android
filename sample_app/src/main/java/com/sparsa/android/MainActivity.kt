package com.sparsa.android

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.sparsa.android.main.ContentView
import com.sparsa.android.main.ContentViewModel
import com.sparsa.android.main.helpers.State
import com.sparsa.android.main.helpers.updateButtonStates
import com.sparsa.android.notifications.NotificationHandler
import com.sparsainc.sdk.sparsa.Sparsa
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val viewModel : ContentViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdge()
        CoroutineScope(Dispatchers.IO).launch {
            setupViewModel()
            setupNotifications()
        }
        setContent {
            ContentView(viewModel)
        }
    }

    private suspend fun setupViewModel() {
        sharedPreferences = this.getSharedPreferences("sample.app", MODE_PRIVATE)
        viewModel.configure(this, getState()) { state ->
            set(state)
        }

    }

    private suspend fun setupNotifications() {
        FirebaseApp.initializeApp(this)
        sendToken()
        Sparsa.handleNotification(this.intent.extras, onDelete = {
            viewModel.clearState()
            viewModel.updateButtonStates()
        }, onError = {
            viewModel.showAlertMessage(it.message ?: "")
        })
        withContext(Dispatchers.Main) {
            NotificationHandler.notification.observe(this@MainActivity) {
                if (it == "delete") {
                    viewModel.clearState()
                    viewModel.updateButtonStates()
                } else {
                    viewModel.showAlertMessage(it)
                }
            }
        }
    }

    private fun getState(): State {
        return Gson().fromJson(
            sharedPreferences.getString("state", Gson().toJson(State())),
            State::class.java
        )
    }

    private fun set(state: String) {
        sharedPreferences.edit { putString("state", state) }
    }

    private fun sendToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = task.result
            Sparsa.updateDeviceToken(token)
        }
    }

    private fun edgeToEdge() {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}