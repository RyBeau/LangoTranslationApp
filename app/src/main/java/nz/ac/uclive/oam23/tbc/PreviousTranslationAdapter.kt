package nz.ac.uclive.oam23.tbc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for the StoredTranslationFragment
 */
class PreviousTranslationAdapter(private var translations: List<Translation>, private val onPreviousTranslationListener: OnPreviousTranslationListener)
    : RecyclerView.Adapter<PreviousTranslationAdapter.PreviousTranslationViewHolder>() {

    enum class SortOrder { ASC , DSC }

    class PreviousTranslationViewHolder(itemView: View, private val onPreviousTranslationListener: OnPreviousTranslationListener)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val date: TextView = itemView.findViewById(R.id.date)
        val originalText: TextView = itemView.findViewById(R.id.originalText)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            onPreviousTranslationListener.onTranslationClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviousTranslationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.previous_translation_item, parent.findViewById(R.id.previousTranslationsViewer), false)
        return PreviousTranslationViewHolder(view, onPreviousTranslationListener)
    }

    override fun onBindViewHolder(viewHolder: PreviousTranslationViewHolder, position: Int) {
        viewHolder.date.text = translations[position].date.toString()
        viewHolder.originalText.text = translations[position].originalText
    }

    override fun getItemCount() = translations.size

    fun setData(newTranslations: List<Translation>) {
        translations = newTranslations
        notifyDataSetChanged()
    }

    fun sortTranslations(order: SortOrder){
        if (order == SortOrder.ASC){
            translations = translations.sortedBy {
                it.date
            }
        } else {
            translations = translations.sortedByDescending {
                it.date
            }
        }
        notifyDataSetChanged()
    }


    interface OnPreviousTranslationListener {
        fun onTranslationClick(position: Int)
    }
}