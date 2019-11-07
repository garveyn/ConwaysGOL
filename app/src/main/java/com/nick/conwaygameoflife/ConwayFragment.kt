package com.nick.conwaygameoflife

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class ConwayFragment : Fragment() {

    // Game state
    var isPlaying:  Boolean = false
    var playSpeed:  Int = 1
    lateinit var state: GameBoard

    //Preferences
    private lateinit var sharedPreferences: SharedPreferences

    // Screen elements
    private lateinit var playPauseButton:   Button
    private lateinit var saveButton:        Button
    private lateinit var loadButton:        Button
    private lateinit var cloneButton:       Button
    private lateinit var recyclerView:      RecyclerView

    companion object {

        // Bundle & intent keys
        const val PLAY_KEY = "play"
        const val BOARD_KEY = "board"
        const val BOARD_SIZE_KEY = "size"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.gol_view, container, false)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Store buttons and setup actions
        // Trying not to use synthetics anymore...
        playPauseButton = view.findViewById(R.id.play_button)
        saveButton = view.findViewById(R.id.save_button)
        loadButton = view.findViewById(R.id.load_button)
        cloneButton = view.findViewById(R.id.clone_button)

        playPauseButton.setOnClickListener {
            updateBoard()
        }

        /*
        Setup Game State - If a board was saved, the board and it's size is in the bundle using the
            elvis operator ( ?: ) that returns the second half if the first is null
        */
        val boardSize = savedInstanceState?.getInt(BOARD_SIZE_KEY)
            ?: sharedPreferences.getString(getString(R.string.gridsize_key), "20")!!.toInt()
        val lifeExpectancy = sharedPreferences.getInt(
            getString(R.string.lifespan_key), GameBoard.IMMORTAL)

        state = GameBoard(boardSize, lifeExpectancy)
        state.board = savedInstanceState?.getParcelableArrayList<Cell>(BOARD_KEY)?.toArray(state.board)
            ?:  Array(boardSize*boardSize) {Cell()}

        // Setup RecyclerView
        recyclerView = view.findViewById<RecyclerView>(R.id.game_screen).apply {
            adapter = ConwayAdapter(state.board, state.size,this@ConwayFragment)
            layoutManager = GridLayoutManager(context, boardSize)
        }

        //TODO use Handler to update state

        return view
    }

    override fun onResume() {
        super.onResume()
        playSpeed = sharedPreferences.getInt(getString(R.string.game_speed_key), 1)
        updateUI()
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
            R.id.action_reset -> {
                resetBoard()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)


        outState.putBoolean(PLAY_KEY, isPlaying)
        outState.putParcelableArrayList(BOARD_KEY, state.board.toMutableList() as ArrayList)
        outState.putInt(BOARD_SIZE_KEY, state.size)

    }

    fun updateUI() {
        val newBoardSize = sharedPreferences.getString(
            getString(R.string.gridsize_key), "20")!!.toInt()
        val newLifeExpectancy = sharedPreferences.getInt(
            getString(R.string.lifespan_key), GameBoard.IMMORTAL)

        if (state.size != newBoardSize) {
            resetBoard()
        }

        state.lifeExpectancy = newLifeExpectancy
    }

    private fun updateBoard() {
        state.calculateNewBoardState()
        for (change in state.changes) {
            recyclerView.adapter?.notifyItemChanged(change)
        }
    }

    fun resetBoard() {
        // Reset board with new size
        val newBoardSize = sharedPreferences.getString(
            getString(R.string.gridsize_key), "20")!!.toInt()
        val newLifeExpectancy = sharedPreferences.getInt(
            getString(R.string.lifespan_key), GameBoard.IMMORTAL)

        state = GameBoard(newBoardSize, newLifeExpectancy)
        val adapter = recyclerView.adapter as ConwayAdapter
        adapter.cellArr = state.board
        adapter.notifyDataSetChanged()
    }

}