package it.progmob.passwordmanager

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.google.firebase.auth.FirebaseAuth
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

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            viewModel.userID = user.uid
        }

        viewModel.fetchDataFromDatabase()

        val userEmail: String? = viewModel.userID
        Toast.makeText(this, userEmail, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host)
        return item.onNavDestinationSelected(navController) ||
                super.onOptionsItemSelected(item)
    }
}