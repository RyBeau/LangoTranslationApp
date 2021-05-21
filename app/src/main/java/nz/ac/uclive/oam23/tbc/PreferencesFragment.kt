package nz.ac.uclive.oam23.tbc

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager


/**
 * A simple [Fragment] subclass.
 * Use the [PreferencesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Loads Shared preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        //Setup a shared preference listener for hpwAddress and restart transport
        val listener: SharedPreferences.OnSharedPreferenceChangeListener =
                SharedPreferences.OnSharedPreferenceChangeListener { _: SharedPreferences, key: String ->
                    if (key == "themes") {
                        setTheme(requireActivity())
                    }
                }
        prefs.registerOnSharedPreferenceChangeListener(listener)

    }





}