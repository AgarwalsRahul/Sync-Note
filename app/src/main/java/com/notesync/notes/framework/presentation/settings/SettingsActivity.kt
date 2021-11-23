package com.notesync.notes.framework.presentation.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.notesync.notes.R
import com.notesync.notes.framework.presentation.BaseActivity

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun inject() {
        TODO("Not yet implemented")
    }
}