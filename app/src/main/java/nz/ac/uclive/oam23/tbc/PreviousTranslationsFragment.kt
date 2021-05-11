package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Fragment for viewing previous translations
 */
class PreviousTranslationsFragment : Fragment(), PreviousTranslationAdapter.OnPreviousTranslationListener {

    private val viewModel: TranslationsViewModel by activityViewModels() {
        TranslationsViewModelFactory((activity?.application as TBCApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.PREVIOUS_TRANSLATIONS)
        val view =  inflater.inflate(R.layout.fragment_previous_translations, container, false)
        val translationAdapter = PreviousTranslationAdapter(listOf(), this)
        val recyclerView = view.findViewById<RecyclerView>(R.id.previousTranslationsViewer)
        recyclerView.apply {
            adapter = translationAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        viewModel.translationsList.observe(viewLifecycleOwner, { newTranslations ->
            translationAdapter.setData(newTranslations)
        })
        return view
    }

    override fun onTranslationClick(position: Int) {
        Toast.makeText(context, "Test", Toast.LENGTH_SHORT).show()
        viewModel.setSelectedIndex(position)
    }
}