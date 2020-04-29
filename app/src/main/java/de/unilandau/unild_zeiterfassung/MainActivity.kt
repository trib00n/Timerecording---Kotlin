package de.unilandau.unild_zeiterfassung

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*
//asüdad

class MainActivity : AppCompatActivity() {


    lateinit var drawer: DrawerLayout
    lateinit var toolbar: Toolbar

    private val fragmentManager = supportFragmentManager
    private val workFragment = WorkFragment()
    private val watchFragment = WatchFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.draw_layout)

        /* Display First Fragment initially */
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, watchFragment)
        fragmentTransaction.commit()



        nav_view.setNavigationItemSelectedListener {
            val fragmentTransaction = fragmentManager.beginTransaction()
            when (it.itemId) {
                R.id.nav_work -> {

                    fragmentTransaction.replace(R.id.fragment_container, workFragment)
                    fragmentTransaction.commit()
                    true
                }
                R.id.nav_watch -> {
                    fragmentTransaction.replace(R.id.fragment_container, watchFragment)
                    fragmentTransaction.commit()
                    true
                }
                R.id.nav_settings -> {
                    fragmentTransaction.replace(R.id.fragment_container, settingsFragment)
                    fragmentTransaction.commit()
                    true
                }
                R.id.nav_help -> {
                    Toast.makeText(this, "Daten übertragen!", Toast.LENGTH_LONG).show()
                    true
                }

                else -> false
            }
        }

        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()



    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}
