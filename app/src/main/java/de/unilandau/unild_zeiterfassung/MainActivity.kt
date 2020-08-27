package de.unilandau.unild_zeiterfassung

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.android.synthetic.main.activity_main.*


@Suppress("NAME_SHADOWING")
class MainActivity : AppCompatActivity() {

    lateinit var drawer: DrawerLayout
    lateinit var toolbar: Toolbar

    private val fragmentManager = supportFragmentManager
    private val editFragment = EditFragment()
    private val watchFragment = WatchFragment()
    private val exportFragment = ExportFragment()

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

        // Löscht die Datenbank
       // this.deleteDatabase("TimeRecordSystem")

        // Wird für Rechte zur Weitergabe von Anhängen benötigt
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())


        // Navigation
        nav_view.setNavigationItemSelectedListener {
            val fragmentTransaction = fragmentManager.beginTransaction()
            when (it.itemId) {
                R.id.nav_work -> {

                    fragmentTransaction.replace(R.id.fragment_container, editFragment)
                    fragmentTransaction.commit()
                    true
                }
                R.id.nav_watch -> {
                    fragmentTransaction.replace(R.id.fragment_container, watchFragment)
                    fragmentTransaction.commit()
                    true
                }
                R.id.nav_settings -> {
                    fragmentTransaction.replace(R.id.fragment_container, exportFragment)
                    fragmentTransaction.commit()
                    true
                }
                R.id.nav_help -> {
                    val addresses = listOf("support@uni-landau.de").toTypedArray()
                    val subject = "Supportanfrage Zeiterfassung"
                    mailtoTypeEmailCreation(addresses,subject)
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

    private fun mailtoTypeEmailCreation(
        addresses: Array<String>, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            val mailto = "mailto:" + addresses.joinToString(",")
            data = Uri.parse(mailto)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
    }


}
