package com.nick.conwaygameoflife

import android.util.Log

class GameBoard(val size: Int, var lifeExpectancy: Int) {
    var board: Array<Cell> = Array(size*size) {Cell()}
    var changes: ArrayList<Int> = ArrayList()

    companion object {
        // Functional Constants
        const val IMMORTAL = 0
        const val SURVIVAL_MIN = 2
        const val SURVIVAL_MAX = 3
        const val BIRTH_NUMBER = 3

        const val TAG = "board"
    }


    fun calculateNewBoardState() {

        val livingCells = getLivingCells()
        val newBoard = board.copyOf()
        val deadCells = arrayListOf<Int>()
        changes = arrayListOf()

        // Calculate deaths
        for (index in livingCells) {
            val cell = board[index]
            var livingNeighbors = 0

            // Handle Age
            if (lifeExpectancy != IMMORTAL) {
                cell.age++
                changes.add(index) // Aging always changes View
                if (cell.age >= lifeExpectancy){
                    // Dies of old age
                    newBoard[index] = Cell()
                    continue
                }
                newBoard[index].age = cell.age
            }

            // Handle Neighbors
            for (neighbor in getNeighbors(index)) {
                if (board[neighbor].isLiving) {
                    livingNeighbors++
                } else if (!deadCells.contains(neighbor)) {
                    deadCells.add(neighbor)
                }
            }

//            Log.d(TAG, "calc deaths --------- \n" +
//                "Cell $cell at ${transTo2D(index)} \n" +
//                "neighbors: ${getNeighbors(index).map {i -> transTo2D(i) }} \n" +
//                "livingNeighbors: $livingNeighbors")

            // Dies if over or under populated
            if (livingNeighbors < SURVIVAL_MIN || livingNeighbors > SURVIVAL_MAX) {
                newBoard[index] = Cell(false, 0)
                //Log.d(TAG, "Cell Killed: ${transTo2D(index)}")
                changes.add(index)
            }
        }

        // Calculate births
        for (index in deadCells) {
            val cell = board[index]
            var livingNeighbors = 0

            for (neighbor in getNeighbors(index)) {
                if (board[neighbor].isLiving) {
                    livingNeighbors++
                }
            }

//            Log.d(TAG, "calc Births ++++++++++++ \n" +
//                    "Cell $cell at ${transTo2D(index)} \n" +
//                    "neighbors: ${getNeighbors(index).map {i -> transTo2D(i) }} \n" +
//                    "livingNeighbors: $livingNeighbors")

            if (livingNeighbors == BIRTH_NUMBER) {
                newBoard[index] = Cell(true, 0)
                changes.add(index)
            }
        }


        // Set new board state
        board = newBoard
//        Log.d(TAG, "From Calc - \n " +
//                "living: ${livingCells.map { i -> transTo2D(i) }} \n \n" +
//                "deadChecked: ${deadCells.map { i -> transTo2D(i) }} \n \n" +
//                "newLiving: ${getLivingCells().map { i -> transTo2D(i) }}")
    }

    private fun getLivingCells() : List<Int> {
        return board.withIndex()
            .filter {( _, cell) -> cell.isLiving }
            .map { (index, _ ) -> index}
    }

    private fun getNeighbors(position: Int) : List<Int> {
        // Due to gridManager, the 1D array needs to be translated to a 2D array
        val (x, y) = transTo2D(position)
        val neighbor2D = listOf(Pair(x+1, y), Pair(x+1, y+1), Pair(x, y+1),
            Pair(x-1, y+1), Pair(x-1, y), Pair(x-1, y-1),
            Pair(x, y-1), Pair(x+1, y-1))

        // Normalize on edges, and translate back to 1D
        return neighbor2D
            .map { (x, y) -> Pair((x + size) % size, (y + size) % size) }
            .map { (x, y) -> transTo1D(x, y) }

    }

    private fun transTo2D(index: Int) = Pair(index % size, index / size)

    private fun transTo1D(x: Int, y: Int) = (y * size) + x


}