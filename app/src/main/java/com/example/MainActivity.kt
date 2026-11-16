package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.LoveConnectDatabase
import com.example.data.local.SessionPreferences
import com.example.data.repository.LoveConnectRepository
import com.example.data.repository.SampleAiRepository
import com.example.ui.navigation.LoveConnectApp
import com.example.ui.viewmodel.LoveConnectViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = LoveConnectDatabase.getInstance(applicationContext)
        val prefs = SessionPreferences(applicationContext)
        val repository = LoveConnectRepository(db, prefs)
        val aiRepository = SampleAiRepository()

        setContent {
            val viewModel: LoveConnectViewModel = viewModel(
                factory = LoveConnectViewModel.Factory(repository, aiRepository)
            )
            LoveConnectApp(viewModel = viewModel)
        }
    }
}
