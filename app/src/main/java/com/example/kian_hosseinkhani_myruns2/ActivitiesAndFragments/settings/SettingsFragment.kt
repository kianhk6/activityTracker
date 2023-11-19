package com.example.kian_hosseinkhani_myruns2.ActivitiesAndFragments.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.kian_hosseinkhani_myruns2.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        // For the webpage link
        findPreference<Preference>("webpage")?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("http://sfu.ca/computing.html")
            startActivity(intent)
            true
        }

        findPreference<Preference>("userProfile")?.setOnPreferenceClickListener {
            val intent = Intent(activity, UserProfileSettings::class.java)
            startActivity(intent)
            true
        }
    }

}
