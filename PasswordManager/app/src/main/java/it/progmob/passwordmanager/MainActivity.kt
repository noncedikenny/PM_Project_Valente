package it.progmob.passwordmanager

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import it.progmob.passwordmanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel : ManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)

        binding.lifecycleOwner = this
        /*
        viewModel.usersList.observe(this) { users ->
            if (users.size == 1) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host, MainFragment())
                    .commit()
            } else if (users.size > 1) {
                setContentView(binding.root)
            }
        }
        */
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