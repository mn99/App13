package com.example.app13

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spanned
import android.text.style.*
import android.util.Patterns
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app13.databinding.ActivityAddNoteBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class TakeNote : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private val model: TakeNoteModel by viewModels()
    private lateinit var adapter: TakeListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedBaseNote = intent.getParcelableExtra<BaseNote>(Constants.SelectedBaseNote)
        if (model.isFirstInstance) {
            if (selectedBaseNote != null) {
                model.isNewNote = false
                model.setStateFromBaseNote(selectedBaseNote)
            } else model.isNewNote = true
            model.isFirstInstance = false
        }
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        if (!model.isNewNote) {
//            binding.AddNoteMoreOptions.visibility = View.GONE
//        } else {
//            noteVisible()
//        }
//        if (selectedBaseNote?.type == Type.LIST) {
//            listVisible()
//        } else {
//            noteVisible()
//        }
        binding.EditTitle.setOnNextAction {
            binding.EditBody.requestFocus()
        }
//        binding.AddNoteMoreOptions.setOnClickListener {
//            val checkboxes = Operation(R.string.checkboxes, R.drawable.checkbox_16) {
//                binding.EditBody.text?.clear()
//                listVisible()
//                addListItem()
//            }
//            val note = Operation(R.string.take_note, R.drawable.edit) {
//                noteVisible()
//            }
//            if (binding.EditBody.isVisible && model.isNewNote) {
//                showMenuActivity(checkboxes)
//            }
//            if (!binding.EditBody.isVisible && model.isNewNote) {
//                showMenuActivity(note)
//            }
//        }
//        binding.AddItemCheckboxes.setOnClickListener {
//            addListItem()
//        }




        setupEditor()
        setupListeners()
        setupToolbar(binding.AddNoteToolbar)
        if (model.isNewNote) {
            binding.EditBody.requestFocus()
        }
        setStateFromModel()
//        setupRecyclerView()
    }
    private fun setupEditor() {
        setupMovementMethod()
        binding.EditBody.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.Bold -> {
                        applySpan(StyleSpan(Typeface.BOLD))
                        mode?.finish()
                    }
                    R.id.Link -> {
                        applySpan(URLSpan(null))
                        mode?.finish()
                    }
                    R.id.Italic -> {
                        applySpan(StyleSpan(Typeface.ITALIC))
                        mode?.finish()
                    }
                    R.id.Monospace -> {
                        applySpan(TypefaceSpan("monospace"))
                        mode?.finish()
                    }
                    R.id.Strikethrough -> {
                        applySpan(StrikethroughSpan())
                        mode?.finish()
                    }
                    R.id.ClearFormatting -> {
                        removeSpans()
                        mode?.finish()
                    }
                }
                return false
            }
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.formatting, menu)
                return true
            }
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

            override fun onDestroyActionMode(mode: ActionMode?) {
                return
            }
        }
    }
    private fun setupListeners() {
        binding.EditTitle.addTextChangedListener(onTextChanged = { text, start, count, after ->
            model.title = text.toString().trim()
        })
        binding.EditBody.addTextChangedListener(afterTextChanged = { editable ->
            model.body = editable
        })
    }
    private fun setStateFromModel() {
        val formatter = BaseNoteModel.getDateFormatter(getLocale())
        binding.EditTitle.setText(model.title)
        binding.EditBody.text = model.body
        binding.TimeCreated.text = formatter.format(model.timestamp)
    }
    private fun setupMovementMethod() {
        val movementMethod = LinkMovementMethod { span ->
            MaterialAlertDialogBuilder(this)
                .setItems(R.array.linkOptions) { dialog, which ->
                    if (which == 1) {
                        val spanStart = binding.EditBody.text?.getSpanStart(span)
                        val spanEnd = binding.EditBody.text?.getSpanEnd(span)

                        ifBothNotNullAndInvalid(spanStart, spanEnd) { start, end ->
                            val text = binding.EditBody.text?.substring(start, end)
                            if (text != null) {
                                val link = getURLFrom(text)
                                val uri = Uri.parse(link)

                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    startActivity(intent)
                                } catch (exception: Exception) {
                                    Toast.makeText(this, R.string.cant_open_link, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }.show()
        }
        binding.EditBody.movementMethod = movementMethod
    }
    private fun removeSpans() {
        val selectionEnd = binding.EditBody.selectionEnd
        val selectionStart = binding.EditBody.selectionStart

        ifBothNotNullAndInvalid(selectionStart, selectionEnd) { start, end ->
            binding.EditBody.text?.getSpans(start, end, CharacterStyle::class.java)?.forEach { span ->
                binding.EditBody.text?.removeSpan(span)
            }
        }
    }
    private fun applySpan(spanToApply: Any) {
        val selectionEnd = binding.EditBody.selectionEnd
        val selectionStart = binding.EditBody.selectionStart

        ifBothNotNullAndInvalid(selectionStart, selectionEnd) { start, end ->
            binding.EditBody.text?.setSpan(spanToApply, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    private fun ifBothNotNullAndInvalid(start: Int?, end: Int?, function: (start: Int, end: Int) -> Unit) {
        if (start != null && start != -1 && end != null && end != -1) {
            function.invoke(start, end)
        }
    }
    companion object {
        fun getURLFrom(text: String): String {
            return when {
                text.matches(Patterns.PHONE.toRegex()) -> "tel:$text"
                text.matches(Patterns.EMAIL_ADDRESS.toRegex()) -> "mailto:$text"
                text.matches(Patterns.DOMAIN_NAME.toRegex()) -> "http://$text"
                else -> text
            }
        }
    }
    override fun onBackPressed() {
        model.saveNote {
            super.onBackPressed()
        }
    }
    private fun setupToolbar(toolbar: MaterialToolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        model.saveNote()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuId = when (model.folder) {
            Folder.NOTES -> R.menu.notes
            Folder.DELETED -> R.menu.deleted
            Folder.ARCHIVED -> R.menu.archived
        }
        menuInflater.inflate(menuId, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.Delete -> deleteNote()
            R.id.Archive -> archiveNote()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun deleteNote() {
        model.moveBaseNoteToDeleted()
        onBackPressed()
    }
    private fun archiveNote() {
        model.moveBaseNoteToArchive()
        onBackPressed()
    }
//    private fun setupRecyclerView() {
//        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
//            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
//                val drag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
//                val swipe = ItemTouchHelper.START or ItemTouchHelper.END
//                return makeMovementFlags(drag, swipe)
//            }
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                model.items.removeAt(viewHolder.adapterPosition)
//                adapter.notifyItemRemoved(viewHolder.adapterPosition)
//            }
//            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
//                Collections.swap(model.items, viewHolder.adapterPosition, target.adapterPosition)
//                adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
//                return true
//            }
//        })
//        adapter = TakeListAdapter(model.items, object : ListItemListener {
//            override fun onMoveToNext(position: Int) {
//                moveToNext(position)
//            }
//            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
//                itemTouchHelper.startDrag(viewHolder)
//            }
//            override fun afterTextChange(position: Int, text: String) {
//                model.items[position].body = text
//            }
//            override fun onCheckedChange(position: Int, checked: Boolean) {
//                model.items[position].checked = checked
//            }
//        })
//        itemTouchHelper.attachToRecyclerView(binding.RecyclerViewCheckboxes)
//        binding.RecyclerViewCheckboxes.adapter = adapter
//        binding.RecyclerViewCheckboxes.layoutManager = LinearLayoutManager(this)
//    }
//    private fun addListItem() {
//        val position = model.items.size
//        val listItem = ListItem(String(), false)
//        model.items.add(listItem)
//        adapter.notifyItemInserted(position)
//        binding.RecyclerViewCheckboxes.post {
//            val viewHolder = binding.RecyclerViewCheckboxes.findViewHolderForAdapterPosition(position) as TakeListViewHolder?
//            viewHolder?.binding?.ListItem?.requestFocus()
//        }
//    }
//    private fun moveToNext(currentPosition: Int) {
//        val viewHolder = binding.RecyclerViewCheckboxes.findViewHolderForAdapterPosition(currentPosition + 1) as TakeListViewHolder?
//        if (viewHolder != null) {
//            if (viewHolder.binding.CheckBox.isChecked) {
//                moveToNext(currentPosition + 1)
//            } else viewHolder.binding.ListItem.requestFocus()
//        } else addListItem()
//    }
//
//    private fun noteVisible() {
//        binding.AddItemCheckboxes.visibility = View.GONE
//        binding.RecyclerViewCheckboxes.visibility = View.GONE
//        binding.EditBody.visibility = View.VISIBLE
//    }
//    private fun listVisible() {
//        binding.AddItemCheckboxes.visibility = View.VISIBLE
//        binding.RecyclerViewCheckboxes.visibility = View.VISIBLE
//        binding.EditBody.visibility = View.GONE
//    }


}