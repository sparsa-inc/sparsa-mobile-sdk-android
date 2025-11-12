package com.sparsa.android.ui

import android.content.Context
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
import main.SparsaMobile
import com.sparsa.android.ui.content.ContentView
import com.sparsa.android.ui.content.ContentViewModel
import com.sparsa.android.ui.notifications.NotificationHandler
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
            ContentView(this, viewModel)
        }
    }

    private suspend fun setupViewModel() {
        sharedPreferences = this.getSharedPreferences("sample.app", Context.MODE_PRIVATE)
        viewModel.configure(this, getState()) { state ->
            set(state)
        }

    }

    private suspend fun setupNotifications() {
        FirebaseApp.initializeApp(this)
        sendToken()
        try {
            SparsaMobile.handleNotification(this.intent.extras) {
                viewModel.clearState()
            }
        } catch (e: Exception) {
            viewModel.showAlertMessage(e.message ?: e.toString())
        }
        withContext(Dispatchers.Main) {
            NotificationHandler.notification.observe(this@MainActivity) {
                viewModel.clearState()
            }
        }
    }

    private fun getState(): ContentViewModel.State {
        return  Gson().fromJson(
            sharedPreferences.getString("state", Gson().toJson(ContentViewModel.State())),
            ContentViewModel.State::class.java
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
            SparsaMobile.updateDeviceToken(token)
        }
    }

    private fun edgeToEdge() {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}