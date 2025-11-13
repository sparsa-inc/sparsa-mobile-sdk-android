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
import com.google.gson.Gson
import com.sparsa.android.ui.content.ContentView
import com.sparsa.android.ui.content.ContentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel : ContentViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdge()
        CoroutineScope(Dispatchers.IO).launch {
            setupViewModel()
        }
        setContent {
            ContentView(viewModel)
        }
    }

    private suspend fun setupViewModel() {
        sharedPreferences = this.getSharedPreferences("sample.app", Context.MODE_PRIVATE)
        viewModel.configure(this, getState()) { state ->
            set(state)
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

    private fun edgeToEdge() {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}