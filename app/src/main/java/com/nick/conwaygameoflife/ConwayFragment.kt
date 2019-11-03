package com.nick.conwaygameoflife

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager

class ConwayFragment : Fragment() {

    // Colors (int Hex Strings)
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
        const val PLAY_KEY = "play"
        const val BOARD_KEY = "board"

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

    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        playSpeed = sharedPreferences.getInt(getString(R.string.game_speed_key), 1)
        backgroundColor = sharedPreferences.getInt(getString(R.string.cc_dead_key),
            ContextCompat.getColor(requireContext(), R.color.default_dark))
        livingColor = sharedPreferences.getInt(getString(R.string.cc_live_key),
            ContextCompat.getColor(requireContext(), R.color.default_light))
        deadColor = sharedPreferences.getInt(getString(R.string.cc_old_key),
            ContextCompat.getColor(requireContext(), R.color.default_light_faded))
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(PLAY_KEY, isPlaying)
        outState.putParcelableArrayList(BOARD_KEY, gameBoard)

    }

}