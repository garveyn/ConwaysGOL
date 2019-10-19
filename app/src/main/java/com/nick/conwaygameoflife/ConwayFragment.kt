package com.nick.conwaygameoflife

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment

class ConwayFragment() : Fragment() {

    // Colors (ints to be casted to Hex Strings)
    var livingColor:        Int = 0
    var deadColor:          Int = 0
    var backgroundColor:    Int = 0

    // Game state
    var isPlaying:  Boolean = false
    var playSpeed:  Int = 1 // May not be used
    var gameBoard:  ArrayList<Cell> = ArrayList(20*20)

    // Buttons
    lateinit var playPauseButton:   Button
    lateinit var saveButton:        Button
    lateinit var loadButton:        Button
    lateinit var cloneButton:       Button

    companion object {

        // Bundle & intent keys

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.gol_view, container, false)

        // Store buttons and setup actions
        // Trying not to use synthetics anymore...
        playPauseButton = view.findViewById(R.id.play_button)
        saveButton = view.findViewById(R.id.save_button)
        loadButton = view.findViewById(R.id.load_button)
        cloneButton = view.findViewById(R.id.clone_button)


        return view
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.settings_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val singleActivity = activity
                if (singleActivity != null && singleActivity is SingleFragmentActivity) {
                    singleActivity.replaceFragment(SettingsFragment())
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}