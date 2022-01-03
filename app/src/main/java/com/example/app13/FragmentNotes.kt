package com.example.app13

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.app13.databinding.FragmentNotesBinding

class FragmentNotes : Fragment(), ItemListener {
    private var binding: FragmentNotesBinding? = null
    private var adapter: BaseNoteAdapter? = null
    private val model: BaseNoteModel by activityViewModels()
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        adapter = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
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
        (requireContext() as MainActivity).binding.appBarMain.fab.setOnClickListener {
            displayNoteTypes()
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
    private fun goToActivity(activity: Class<*>, baseNote: BaseNote? = null) {
        val intent = Intent(requireContext(), activity)
        intent.putExtra(Constants.SelectedBaseNote, baseNote)
        startActivity(intent)
    }
    private fun showOperations(baseNote: BaseNote) {
        val delete = Operation(R.string.delete, R.drawable.delete) { model.moveBaseNoteToDeleted(baseNote.id) }
        val archive = Operation(R.string.archive, R.drawable.archive) { model.moveBaseNoteToArchive(baseNote.id) }
        showMenu(delete, archive)
    }
    private fun setupRecyclerView() {
        binding?.RecyclerView?.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
    }
    private fun getObservable() = model.baseNotes
    private fun setupObserver() {
        getObservable().observe(viewLifecycleOwner, { list ->
            adapter?.submitList(list)
            binding?.RecyclerView?.isVisible = list.isNotEmpty()
        })
    }
    private fun displayNoteTypes() {
        val takeNote = Operation(R.string.take_note, R.drawable.edit) { goToActivity(TakeNote::class.java) }
        showMenu(takeNote)
    }
}