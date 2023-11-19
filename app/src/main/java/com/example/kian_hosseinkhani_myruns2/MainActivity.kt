package com.example.kian_hosseinkhani_myruns2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.history.HistoryFragment
import com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.settings.SettingsFragment
import com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.start.StartFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.ArrayList

// MainActivity.kt
// TO DO:
// Add task bar on top
// give it to teammates to test phase 1

class MainActivity : AppCompatActivity() {
    private lateinit var startFragment: StartFragment
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var historyFragment: HistoryFragment


    // object for the tab view pager
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var tabsPagerAdapter: TabsPagerAdapter

    //items in arrayList are fragments
    private lateinit var fragments: ArrayList<Fragment>

    // title of the tabs into an array
    private val tabTitles = arrayOf("Start", "History", "Settings")

    private lateinit var tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy

    //  tabLayoutMediator: associate each tab item to each fragment
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Runs"

        viewPager2 = findViewById(R.id.viewpager)
        tabLayout = findViewById(R.id.tab)

        // initialize objects
        startFragment = StartFragment()
        settingsFragment = SettingsFragment()
        historyFragment = HistoryFragment()

        // add fragments to arrayList
        fragments = ArrayList()
        fragments.add(startFragment)
        fragments.add(historyFragment)
        fragments.add(settingsFragment)

        //creating an adapter for the fragments above
        // input: context, list (arrayList)
        tabsPagerAdapter = TabsPagerAdapter(this, fragments)
        viewPager2.adapter = tabsPagerAdapter


        // showing the titles
        // everytime when u tab the path object the position
        // for first tab tab.text --> array: tab titles 1
        // for second tab tab.text --> array:tab titles 2
        tabConfigurationStrategy =
            TabLayoutMediator.TabConfigurationStrategy {
                    tab: TabLayout.Tab, position: Int ->
                tab.text = tabTitles[position]
            }

        // inputs: tab, viewPager, tabConfigurationStrategy (handles title of the text)
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabLayoutMediator.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
    }
}