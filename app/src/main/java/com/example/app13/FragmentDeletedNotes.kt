package com.example.app13

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.app13.databinding.FragmentNotesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FragmentDeletedNotes : Fragment(), ItemListener {
    private var binding: FragmentNotesBinding? = null
    private var adapter: BaseNoteAdapter? = null
    private val model: BaseNoteModel by activityViewModels()
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        adapter = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = BaseNoteAdapter(this)
        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (itemCount > 0) {
                    binding?.RecyclerView?.scrollToPosition(positionStart)
                }
            }
        })
        binding?.RecyclerView?.adapter = adapter
        binding?.RecyclerView?.setHasFixedSize(true)
        setupRecyclerView()
        setupObserver()
        setHasOptionsMenu(true)
    }
    private fun setupRecyclerView() {
        binding?.RecyclerView?.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
    }
    private fun getObservable() = model.deletedNotes
    private fun setupObserver() {
        getObservable().observe(viewLifecycleOwner) { list ->
            adapter?.submitList(list)
            binding?.RecyclerView?.isVisible = list.isNotEmpty()
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNotesBinding.inflate(inflater)
        return binding?.root
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.RequestCodeExportFile && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                model.writeCurrentFileToUri(uri)
            }
        }
    }
    override fun onClick(position: Int) {
        adapter?.currentList?.get(position)?.let { baseNote ->
            when (baseNote.type) {
                Type.NOTE -> goToActivity(TakeNote::class.java, baseNote)
//                Type.LIST -> goToActivity(MakeList::class.java, baseNote)
            }
        }
    }
    override fun onLongClick(position: Int) {
        adapter?.currentList?.get(position)?.let { baseNote -> showOperations(baseNote) }
    }
    private fun confirmDeletionOfAllNotes() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_all_notes)
            .setPositiveButton(R.string.delete) { dialog, which ->
                model.deleteAllBaseNotes()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    private fun confirmDeletionOfSingleNote(baseNote: BaseNote) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_note_forever)
            .setPositiveButton(R.string.delete) { dialog, which ->
                model.deleteBaseNoteForever(baseNote)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    private fun showOperations(baseNote: BaseNote) {
        val restore = Operation(R.string.restore, R.drawable.restore) { model.restoreBaseNote(baseNote.id) }
        val deleteForever = Operation(R.string.delete_forever, R.drawable.delete) { confirmDeletionOfSingleNote(baseNote) }
        showMenu(restore, deleteForever)
    }
    private fun goToActivity(activity: Class<*>, baseNote: BaseNote? = null) {
        val intent = Intent(requireContext(), activity)
        intent.putExtra(Constants.SelectedBaseNote, baseNote)
        startActivity(intent)
    }
}