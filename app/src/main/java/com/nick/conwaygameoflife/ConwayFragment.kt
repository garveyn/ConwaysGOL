package com.nick.conwaygameoflife

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
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
        const val TAG = "ConwayFragment"

        // Request codes
        const val FILE_TO_WRITE_RC = 0
        const val FILE_TO_READ_RC = 1

        // Functional Constants
        const val DEFAULT_FILENAME = "filename"
        const val MIME_TYPE = "application/JSON"

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
        initializeButtons(view)


        /*
        Setup Game State - If a board was saved, the board and it's size is in the bundle using the
            elvis operator ( ?: ) that returns the second half if the first is null
        */
        val boardSize = savedInstanceState?.getInt(BOARD_SIZE_KEY)
            ?: sharedPreferences.getString(getString(R.string.gridsize_key), "20")!!.toInt()
        val lifeExpectancy = sharedPreferences.getString(
            getString(R.string.lifespan_key), GameBoard.IMMORTAL.toString())!!.toInt()

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

    fun initializeButtons(view: View) {
        // Trying not to use synthetics anymore...
        playPauseButton = view.findViewById(R.id.play_button)
        saveButton = view.findViewById(R.id.save_button)
        loadButton = view.findViewById(R.id.load_button)
        cloneButton = view.findViewById(R.id.clone_button)

        playPauseButton.setOnClickListener {
            updateBoard()
        }

        saveButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = MIME_TYPE
                putExtra(Intent.EXTRA_TITLE, DEFAULT_FILENAME)
            }

            startActivityForResult(intent, FILE_TO_WRITE_RC)
        }

        loadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)

                type = MIME_TYPE
            }
            Log.d(TAG, "Pre-pre-Reading!")

            startActivityForResult(intent, FILE_TO_READ_RC)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Log.d(TAG, "requestCode: $requestCode \n" +
                "resultCode: $resultCode \n" +
                "data: $data")

        if (requestCode == FILE_TO_WRITE_RC && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                val json = Gson().toJson(state)
                val fd = activity?.contentResolver
                    ?.openFileDescriptor(uri, "w")?.fileDescriptor

                FileOutputStream(fd).use {
                    it.write(json.toByteArray())
                }
            }
        } else if (requestCode == FILE_TO_READ_RC && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Pre-Reading!")
            data?.data?.also { uri ->
                val fd = activity?.contentResolver
                    ?.openFileDescriptor(uri, "r")?.fileDescriptor



                JsonReader(FileReader(fd)).use {
                    Log.d(TAG, state.toString())
                    it.isLenient = true
                    state = Gson().fromJson(it, GameBoard::class.java)
                }

                Log.d(TAG, state.toString())

                activity?.getPreferences(Context.MODE_PRIVATE)?.edit {
                    putString(getString(R.string.gridsize_key), state.size.toString())
                    putInt(getString(R.string.lifespan_key), state.lifeExpectancy)
                }

                (recyclerView.adapter as ConwayAdapter).cellArr = state.board
                recyclerView.adapter!!.notifyDataSetChanged()

            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
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
        val newLifeExpectancy = sharedPreferences.getString(
            getString(R.string.lifespan_key), GameBoard.IMMORTAL.toString())!!.toInt()

        if (state.size != newBoardSize) {
            resetBoard()
        }

        state.lifeExpectancy = newLifeExpectancy
    }

    private fun updateBoard() {
        state.calculateNewBoardState()
        val adapter = recyclerView.adapter as? ConwayAdapter
        adapter?.cellArr = state.board
        for (change in state.changes) {
            adapter?.notifyItemChanged(change)
        }
    }

    fun resetBoard() {
        // Reset board with new size
        val newBoardSize = sharedPreferences.getString(
            getString(R.string.gridsize_key), "20")!!.toInt()
        val newLifeExpectancy = sharedPreferences.getString(
            getString(R.string.lifespan_key), GameBoard.IMMORTAL.toString())!!.toInt()

        state = GameBoard(newBoardSize, newLifeExpectancy)
        val adapter = recyclerView.adapter as ConwayAdapter
        adapter.cellArr = state.board
        adapter.notifyDataSetChanged()
    }

}