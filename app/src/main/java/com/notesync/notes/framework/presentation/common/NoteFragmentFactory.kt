package com.notesync.notes.framework.presentation.common

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.framework.presentation.notedetail.NoteDetailFragment
import com.notesync.notes.framework.presentation.notelist.NoteListFragment
import com.notesync.notes.framework.presentation.splash.SplashFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@ObsoleteCoroutinesApi
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
                val fragment = NoteDetailFragment(viewModelFactory)
                fragment
            }

            SplashFragment::class.java.name -> {
                val fragment = SplashFragment(viewModelFactory)
                fragment
            }

            else -> {
                super.instantiate(classLoader, className)
            }
        }
}