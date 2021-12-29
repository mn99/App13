package com.example.app13

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FragmentDeletedNotesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Deleted Fragment"
    }
    val text: LiveData<String> = _text
}