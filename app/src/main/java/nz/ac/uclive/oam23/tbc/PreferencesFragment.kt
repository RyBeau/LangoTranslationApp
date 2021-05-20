package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceFragmentCompat


/**
 * A simple [Fragment] subclass.
 * Use the [PreferencesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }


}