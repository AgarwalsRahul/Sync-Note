package com.notesync.notes.framework.presentation.trash


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.notesync.notes.R
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.framework.presentation.common.gone
import com.notesync.notes.framework.presentation.common.visible
import com.notesync.notes.util.printLogD
import kotlinx.android.synthetic.main.layout_note_list_item.view.*


class TrashListAdapter(
    private val interaction: Interaction? = null,
    private val lifecycleOwner: LifecycleOwner,
    private val selectedNotes: LiveData<ArrayList<Note>>,
    private val dateUtil: DateUtil
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {

        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return TrashViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_note_list_item,
                parent,
                false
            ),
            interaction,
            lifecycleOwner,
            selectedNotes,
            dateUtil
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TrashViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Note>) {
        val commitCallback = Runnable {
            // if process died must restore list position
            // very annoying
            interaction?.restoreListPosition()
        }
        printLogD("listadapter", "size: ${list.size}")
        differ.submitList(list, commitCallback)
    }

    fun getNote(index: Int): Note? {
        return try {
            differ.currentList[index]
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            null
        }
    }

    class TrashViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        private val lifecycleOwner: LifecycleOwner,
        private val selectedNotes: LiveData<ArrayList<Note>>,
        private val dateUtil: DateUtil
    ) : RecyclerView.ViewHolder(itemView) {



        private lateinit var note: Note

        fun bind(item: Note) = with(itemView) {
            setOnClickListener {
                interaction?.onItemSelected(adapterPosition, note)
            }
            setOnLongClickListener {
                interaction?.activateMultiSelectionMode()
                interaction?.onItemSelected(adapterPosition, note)
                true
            }
            note = item

//
            findViewById<TextView>(R.id.note_title).setText(item.title)
            findViewById<TextView>(R.id.note_body_card).setText(item.body)
            findViewById<TextView>(R.id.note_timestamp).setText(
                dateUtil.removeTimeFromDateString(
                    item.updated_at
                )
            )


            selectedNotes.observe(lifecycleOwner, Observer { notes ->

                if (notes != null) {
                    if (notes.contains(note)) {
                        this.check_mark.visible()
                    } else {
                        this.check_mark.gone()
                    }
                } else {
                    this.check_mark.gone()
                }
            })
        }
    }


    interface Interaction {

        fun onItemSelected(position: Int, item: Note)

        fun restoreListPosition()

        fun isMultiSelectionModeEnabled(): Boolean

        fun activateMultiSelectionMode()

        fun isNoteSelected(note: Note): Boolean
    }

}