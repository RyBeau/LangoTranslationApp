package nz.ac.uclive.oam23.tbc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Fragment for viewing previous translations
 */
class PreviousTranslationsFragment : NavFragment(), PreviousTranslationAdapter.OnPreviousTranslationListener {

    private val viewModel: TranslationsViewModel by activityViewModels {
        TranslationsViewModelFactory((activity?.application as TBCApplication).repository)
    }

    private lateinit var currentSort: PreviousTranslationAdapter.SortOrder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = activity as MainActivity
        mainActivity.setLocation(MainActivity.Location.PREVIOUS_TRANSLATIONS)

        val view =  inflater.inflate(R.layout.fragment_previous_translations, container, false)
        val translationAdapter = PreviousTranslationAdapter(listOf(), this)
        val recyclerView = view.findViewById<RecyclerView>(R.id.previousTranslationsViewer)

        viewModel.translationsList.observe(viewLifecycleOwner, { newTranslations ->
            translationAdapter.setData(newTranslations)
            translationAdapter.sortTranslations(currentSort)
        })

        recyclerView.apply {
            adapter = translationAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        val toggleButton = view.findViewById<ToggleButton>(R.id.dateButton)
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            setSort(isChecked, translationAdapter)
        }
        setSort(toggleButton.isChecked, translationAdapter)
        return view
    }

    override fun onTranslationClick(position: Int) {
        viewModel.setSelectedIndex(position)
        val bundle = bundleOf("translationKey" to (viewModel.translationsList.value!![position].id))
        findNavController().navigate(R.id.action_navigation_previous_to_navigation_viewTranslation, bundle)
    }

    private fun setSort(isChecked: Boolean, adapter: PreviousTranslationAdapter){
        currentSort = if (isChecked) {
            PreviousTranslationAdapter.SortOrder.ASC
        } else {
            PreviousTranslationAdapter.SortOrder.DSC
        }
        adapter.sortTranslations(currentSort)
    }
}