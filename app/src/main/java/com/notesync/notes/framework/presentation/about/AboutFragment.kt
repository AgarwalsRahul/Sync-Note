package com.notesync.notes.framework.presentation.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.notesync.notes.R
import com.notesync.notes.framework.presentation.common.displayToast
import com.notesync.notes.util.Constants
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        rate.setOnClickListener {
            rate()
        }
    }


    private fun setupToolbar(){
        about_toolbar.title="About"
        about_toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    fun rate() {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(Constants.PLAYSTORE_LINK)
        )
        if (browserIntent.resolveActivity(requireActivity().packageManager) != null)
            startActivity(browserIntent)
        else
            activity?.displayToast(getString(R.string.no_browser))
    }


}