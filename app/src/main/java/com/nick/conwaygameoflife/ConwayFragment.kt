package com.nick.conwaygameoflife

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.util.ArrayList

class ConwayFragment : Fragment() {

    // Game state
    var isPlaying:  Boolean = false
    var playSpeed:  Int = 1
    lateinit var state: GameBoard
    lateinit var updater: UiUpdater

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
        const val BOARD_KEY = "board"
        const val TAG = "ConwayFragment"

        // Request codes
        const val FILE_TO_WRITE_RC = 0
        const val FILE_TO_READ_RC = 1

        // Functional Constants
        const val DEFAULT_FILENAME = "filename"
        const val MIME_TYPE = "application/JSON"
        const val BASE_SPEED = 200

        fun newInstance(boardString: String) : ConwayFragment {
            val args = Bundle().apply {
                putString(BOARD_KEY, boardString)
            }
            return ConwayFragment().apply {
                arguments = args
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // load from bundle
        val boardSize = sharedPreferences.getString(
            getString(R.string.gridsize_key), "20")!!.toInt()
        val lifeExpectancy = sharedPreferences.getString(
            getString(R.string.lifespan_key), GameBoard.IMMORTAL.toString())!!.toInt()
        val json = savedInstanceState?.getString(BOARD_KEY) ?: arguments?.getString(BOARD_KEY)

        state = if (json.isNullOrEmpty()) {
            GameBoard(boardSize, lifeExpectancy)
        } else {
            Gson().fromJson(json, GameBoard::class.java)
        }

        if (state.size != boardSize) {
            state = GameBoard(boardSize, lifeExpectancy)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.gol_view, container, false)

        // Store buttons and setup actions
        initializeButtons(view)

        // Goes from 0-10, so add one to avoid the 0
        playSpeed = sharedPreferences.getInt(getString(R.string.game_speed_key), 0) + 1
        updater = UiUpdater(Runnable { updateBoard() }, (playSpeed * BASE_SPEED).toLong())

        // Setup RecyclerView
        recyclerView = view.findViewById<RecyclerView>(R.id.game_screen).apply {
            adapter = ConwayAdapter(state.board, state.size, this@ConwayFragment)
            layoutManager = GridLayoutManager(context, state.size)
            setBackgroundColor(Color.parseColor("#ffffff"))
        }

        return view
    }


    fun initializeButtons(view: View) {
        playPauseButton = view.findViewById(R.id.play_button)
        saveButton = view.findViewById(R.id.save_button)
        loadButton = view.findViewById(R.id.load_button)
        cloneButton = view.findViewById(R.id.clone_button)

        playPauseButton.setOnClickListener {
            if (!isPlaying) {
                updater.startUpdates()
                playPauseButton.text = getString(R.string.pause_button)
                isPlaying = true
            } else {
                updater.stopUpdates()
                playPauseButton.text = getString(R.string.play_button)
                isPlaying = false
            }
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

        cloneButton.setOnClickListener {
            val singleActivity = activity
            if (singleActivity != null && singleActivity is SingleFragmentActivity) {
                Toast.makeText(requireContext(), R.string.cloned_toast, Toast.LENGTH_LONG).show()
                singleActivity.replaceFragment(newInstance(Gson().toJson(state)))
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val newSpeed = sharedPreferences.getInt(getString(R.string.game_speed_key), 1)
        if (newSpeed == playSpeed) {
            playSpeed = newSpeed
            updater.interval = playSpeed.toLong()
        }
        if (isPlaying){
            updater.startUpdates()
            playPauseButton.text = getString(R.string.pause_button)
        }
        updateUI()
    }

    override fun onPause() {
        super.onPause()
        updater.stopUpdates()
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
            R.id.action_share -> {
                shareBoard()
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

        val json = Gson().toJson(state)
        outState.putString(BOARD_KEY, json)

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
        recyclerView.layoutManager = GridLayoutManager(requireContext(), newBoardSize)
        val adapter = recyclerView.adapter as ConwayAdapter
        adapter.cellArr = state.board
        adapter.notifyDataSetChanged()
    }

    /** *
     * Inspired from the following sources:
     * https://stackoverflow.com/a/32829056/12369045
     * https://stackoverflow.com/a/50924037/12369045
     */
    fun shareBoard() {

        // Get board bitmap
        recyclerView.measure(
            View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))

        val bm = Bitmap.createBitmap(recyclerView.width, recyclerView.height, Bitmap.Config.ARGB_8888)
        recyclerView.draw(Canvas(bm))

        // Store bitmap
        val path = requireContext().cacheDir
        path.mkdirs()
        val file = File(path, "fileToShare.png")
        val fOut = FileOutputStream(file)
        bm.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        fOut.close()


        // Get URI
        val uri = FileProvider.getUriForFile(
            requireContext(),requireContext().applicationContext.packageName, file)

        // Start sharing activity
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(Intent.createChooser(intent, "Choose an App:"))
    }

}