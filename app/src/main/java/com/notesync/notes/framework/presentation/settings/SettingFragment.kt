package com.notesync.notes.framework.presentation.settings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.navigation.NavigationView
import com.notesync.notes.R
import com.notesync.notes.framework.dataSource.cache.database.NOTE_FILTER_DATE_CREATED
import com.notesync.notes.framework.dataSource.cache.database.NOTE_FILTER_TITLE
import com.notesync.notes.framework.presentation.MainActivity
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_trash.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class SettingFragment : Fragment() {



    private var dialog:MaterialDialog?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        theme_option.setOnClickListener {
            (activity as SettingsActivity).showThemeDialog()
        }
    }



    private fun setupToolbar() {
        setting_toolbar.title = "Settings"
        setting_toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
    }

}