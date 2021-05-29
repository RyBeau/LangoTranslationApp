package nz.ac.uclive.oam23.tbc

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

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
                    when (key) {
                        "themes" -> activity?.let { setTheme(it) }
                        "notifications" -> activity?.let { setNotifications(it) }
                    }
                }
        prefs.registerOnSharedPreferenceChangeListener(listener)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.PREFERENCES)
        return super.onCreateView(inflater, container, savedInstanceState)
    }



}