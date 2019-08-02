package com.koinwarga.android.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.koinwarga.android.R
import com.koinwarga.android.ui.dashboard.DashboardFragment
import com.koinwarga.android.ui.history.HistoryFragment
import com.koinwarga.android.ui.manage_account.ManageAccountActivity
import com.koinwarga.android.ui.send.SendFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        val btnChangeAccount = navView.getHeaderView(0).findViewById<Button>(R.id.btnChangeAccount)
        btnChangeAccount.setOnClickListener {
            goToManageAccountPage()
        }

        showDashboardPage()
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.navDashboard -> {
                showDashboardPage()
            }
            R.id.navSend -> {
                showSendPage()
            }
            R.id.navHistory -> {
                showHistoryPage()
            }
            R.id.navRegisteringAccount -> {
                showRegisteringAccountPage()
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun goToManageAccountPage() {
        Intent(this, ManageAccountActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun showDashboardPage() {
        val dashboardFragment = DashboardFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.vContainer, dashboardFragment, "DashboardFragment")
            .commit()
    }

    private fun showSendPage() {
        val sendFragment = SendFragment.newInstance(false)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.vContainer, sendFragment, "SendFragment")
            .commit()
    }

    private fun showHistoryPage() {
        val historyFragment = HistoryFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.vContainer, historyFragment, "HistoryFragment")
            .commit()
    }

    private fun showRegisteringAccountPage() {
        val sendFragment = SendFragment.newInstance(true)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.vContainer, sendFragment, "RegisteringAccountFragment")
            .commit()
    }
}
