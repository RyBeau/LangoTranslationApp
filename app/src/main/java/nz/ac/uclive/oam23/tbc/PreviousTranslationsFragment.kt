package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate

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

        view.findViewById<Button>(R.id.addFillerTranslationButton).setOnClickListener {
            val translation = Translation("temp original text", "temp translated text", LocalDate.now(), "temp note", LatLng(0.0, 0.0), "Note")
            viewModel.addTranslation(translation)
        }

        return view
    }

    override fun onTranslationClick(position: Int) {
        viewModel.setSelectedIndex(position)
        val bundle = bundleOf("translationKey" to (viewModel.translationsList.value!![position].id))
        requireView().findNavController().navigate(R.id.action_navigation_previous_to_navigation_viewTranslation, bundle)
    }
}