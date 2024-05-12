package it.progmob.passwordmanager

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import it.progmob.passwordmanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: ManagerViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)

        binding.lifecycleOwner = this
        viewModel.fetchDataFromDatabase()
    }
}