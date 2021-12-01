package com.notesync.notes.framework.presentation.common

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.di.main.MainScope
import com.notesync.notes.framework.presentation.changePassword.ChangePasswordFragment
import com.notesync.notes.framework.presentation.notedetail.NoteDetailFragment
import com.notesync.notes.framework.presentation.notelist.NoteListFragment
import com.notesync.notes.framework.presentation.trash.TrashFragment
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
@MainScope
class NoteFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
): FragmentFactory(){

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when(className){

            NoteListFragment::class.java.name -> {
                val fragment = NoteListFragment(viewModelFactory, dateUtil)
                fragment
            }

            NoteDetailFragment::class.java.name -> {
                val fragment = NoteDetailFragment(viewModelFactory,dateUtil)
                fragment
            }
            TrashFragment::class.java.name->{
                val fragment = TrashFragment(viewModelFactory,dateUtil)
                fragment
            }

            ChangePasswordFragment::class.java.name->{
                ChangePasswordFragment(viewModelFactory)
            }

            else -> {
                super.instantiate(classLoader, className)
            }
        }
}