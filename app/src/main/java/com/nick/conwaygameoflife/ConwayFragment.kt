package com.nick.conwaygameoflife

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

class ConwayFragment() : Fragment() {

    // Colors (ints to be casted to Hex Strings)
    var livingColor:        Int = 0
    var deadColor:          Int = 0
    var backgroundColor:    Int = 0

    // Game state
    var isPlaying:          Boolean = false
    var playSpeed:          Int = 1 // May not be used
    var gameBoard:          ArrayList<Cell> = ArrayList(20*20)

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


        return view
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.settings_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                //TODO Go to settings screen

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}