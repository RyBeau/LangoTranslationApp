package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

open class NoNavFragment: Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).isVisible = false
        super.onViewCreated(view, savedInstanceState)
    }
}