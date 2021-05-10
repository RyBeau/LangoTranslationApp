package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Fragment for viewing previous translations
 */
class PreviousTranslationsFragment : Fragment(), PreviousTranslationAdapter.OnPreviousTranslationListener {

    private val tempTranslationsList = arrayOf<PreviousTranslation>(
        PreviousTranslation("1/1/2021", "Test Text"),
        PreviousTranslation("2/1/2021", "Test Text"),
        PreviousTranslation("3/1/2021", "Test Text Longer"),
        PreviousTranslation("4/1/2021", "Test Text"),
        PreviousTranslation("5/1/2021", "Test Text"),
        PreviousTranslation("6/1/2021", "Test Text Longer"),
        PreviousTranslation("7/1/2021", "Test Text"),
        PreviousTranslation("8/1/2021", "Test Text"),
        PreviousTranslation("9/1/2021", "Test Text Longest Text of all"),
        PreviousTranslation("10/1/2021", "Test Text")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.PREVIOUS_TRANSLATIONS)
        val view =  inflater.inflate(R.layout.fragment_previous_translations, container, false)
        val translationAdapter = PreviousTranslationAdapter(tempTranslationsList, this)
        val recyclerView = view.findViewById<RecyclerView>(R.id.previousTranslationsViewer)
        recyclerView.apply {
            adapter = translationAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        return view
    }

    override fun onTranslationClick(position: Int) {
        Toast.makeText(context, "Test", Toast.LENGTH_SHORT).show()
    }
}