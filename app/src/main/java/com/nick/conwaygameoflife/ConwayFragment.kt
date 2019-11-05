package com.nick.conwaygameoflife

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ConwayFragment : Fragment() {

    // Game state
    var isPlaying:  Boolean = false
    var playSpeed:  Int = 1 // May not be used
    var gameBoard:  ArrayList<Cell> = ArrayList(20*20)
    var changes: ArrayList<Int> = ArrayList()

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

        // Functional Constants
        const val IMMORTAL = 0
        const val SURVIVAL_MIN = 2
        const val SURVIVAL_MAX = 3
        const val BIRTH_NUMBER = 3

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

        // Setup RecyclerView
        val boardSize = sharedPreferences.getString(
            getString(R.string.gridsize_key), "20")!!.toInt()

        recyclerView = view.findViewById<RecyclerView>(R.id.game_screen).apply {
            adapter = ConwayAdapter(gameBoard, context)
            layoutManager = GridLayoutManager(context, boardSize)
        }


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
                resetBoard(sharedPreferences.getString(
                    getString(R.string.gridsize_key), "20")!!.toInt())
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

    fun updateUI() {
        val newBoardSize = sharedPreferences.getString(
            getString(R.string.gridsize_key), "20")!!.toInt()

        if (gameBoard.size != newBoardSize*newBoardSize) {
            // Reset board with new size
            resetBoard(newBoardSize)
        }
    }

    private fun updateBoard() {
        gameBoard = calculateNewBoardState()
        for (change in changes) {
            recyclerView.adapter?.notifyItemChanged(change)
        }
    }

    private fun resetBoard(newBoardSize: Int) {
        gameBoard = ArrayList(newBoardSize*newBoardSize)
        val adapter = recyclerView.adapter as ConwayAdapter
        adapter.cellArr = gameBoard
        adapter.notifyDataSetChanged()
    }

    private fun calculateNewBoardState() : ArrayList<Cell> {
        val lifeExpencancy = sharedPreferences.getInt(getString(R.string.lifespan_key), 0)
        val livingCells = getLivingCells()
        val newBoard = gameBoard
        val deadCells = mutableSetOf<Int>()

        // Calculate deaths
        for (index in livingCells) {
            val cell = gameBoard[index]
            var livingNeighbors = 0

            // Handle Age
            if (lifeExpencancy != 0) {
                cell.age++
                changes.add(index) // Aging always changes View
                if (cell.age >= lifeExpencancy){
                    // Dies of old age
                    newBoard[index] = Cell()
                    continue
                }
                newBoard[index].age = cell.age
            }

            // Handle Neighbors
            for (neighbor in getNeighbors(index)) {
                if (gameBoard[neighbor].isLiving) {
                    livingNeighbors++
                } else if (!deadCells.contains(neighbor)) {
                    deadCells.add(neighbor)
                }
            }

            // Dies if over or under populated
            if (livingNeighbors < SURVIVAL_MIN || livingNeighbors > SURVIVAL_MAX) {
                newBoard[index] = Cell()
                changes.add(index)
            }
        }

        // Calculate births
        for (index in deadCells) {
            val cell = gameBoard[index]
            var livingNeighbors = 0

            for (neighbor in getNeighbors(index)) {
                if (gameBoard[neighbor].isLiving) {
                    livingNeighbors++
                }
            }

            if (livingNeighbors == BIRTH_NUMBER) {
                cell.isLiving = true
                newBoard[index] = cell
                changes.add(index)
            }
        }

        // Return new board state
        return newBoard
    }

    private fun getLivingCells() : List<Int> {
        return gameBoard.withIndex()
            .filter {( _, cell) -> cell.isLiving }
            .map { (index, _ ) -> index}
    }

    private fun getNeighbors(position: Int) : List<Int> {
        val boardSize = sharedPreferences.getString(
            getString(R.string.gridsize_key), "20")!!.toInt()

        // Due to gridManager, the 1D array needs to be translated to a 2D array
        val (x, y) = transTo2D(position, boardSize)
        val neighbor2D = listOf(Pair(x+1, y), Pair(x+1, y+1), Pair(x, y+1),
            Pair(x-1, y+1), Pair(x-1, y), Pair(x-1, y-1),
            Pair(x, y-1), Pair(x+1, y-1))

        // Normalize on edges, and translate back to 1D
        return neighbor2D
            .map { (x, y) -> Pair((x + boardSize) % boardSize, (y + boardSize) % boardSize) }
            .map { (x, y) -> transTo1D(x, y, boardSize) }

    }

    private fun transTo2D(index: Int, boardSize: Int) = Pair(index % boardSize, index / boardSize)

    private fun transTo1D(x: Int, y: Int, boardSize: Int) = (y * boardSize) + x

}