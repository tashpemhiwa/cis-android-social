package com.tinashe.hymnal.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.tinashe.hymnal.R
import com.tinashe.hymnal.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AppBarBehaviour {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.apply {
            val navController = findNavController(R.id.nav_host_fragment)
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_hymns,
                    R.id.navigation_collections,
                    R.id.navigation_search,
                    R.id.navigation_info
                )
            )
            navView.setupWithNavController(navController)
            toolbarLayout.setupWithNavController(
                binding.toolbar,
                navController,
                appBarConfiguration
            )
        }
    }

    override fun setAppBarExpanded(expanded: Boolean) {
        binding.appBarLayout.setExpanded(expanded, true)
    }

    override fun setAppBarTitle(title: String) {
        binding.toolbarLayout.title = title
    }
}
