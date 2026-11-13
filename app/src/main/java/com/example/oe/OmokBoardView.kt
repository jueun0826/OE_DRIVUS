package com.example.oe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class OmokBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 17x17 정밀 오목 바둑판 스케일
    private val boardSize = 17
    private val boardState = Array(boardSize) { IntArray(boardSize) { 0 } } // 0: 빈칸, 1: 흑돌(내 차), 2: 백돌(상대방)

    // 색상 설정 (연한 베이지 바둑판 + 갈색/검정 사이 격자선)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE8D2B4.toInt() // 연한 베이지 나무 질감 색상
        style = Paint.Style.FILL
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3D2612.toInt() // 갈색-검정 사이 어두운 나무 격자선 색상
        strokeWidth = 2.5f
        style = Paint.Style.STROKE
    }

    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3D2612.toInt()
        style = Paint.Style.FILL
    }

    private val blackStonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF18181B.toInt() // 흑돌
        style = Paint.Style.FILL
    }

    private val blackStoneStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF09090B.toInt()
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val whiteStonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFAFAFA.toInt() // 백돌
        style = Paint.Style.FILL
    }

    private val whiteStoneStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFA1A1AA.toInt()
        strokeWidth = 2.5f
        style = Paint.Style.STROKE
    }

    var isUserTurn = true // true: 흑돌(나), false: 백돌(상대방)
    var isGameOver = false

    var onTurnChangeListener: ((isUserTurn: Boolean, statusText: String) -> Unit)? = null

    fun resetBoard() {
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                boardState[r][c] = 0
            }
        }
        isUserTurn = true
        isGameOver = false
        invalidate()
    }

    // 창의 사용 가능한 최대 높이에 맞춰 1:1 정사각형으로 자동 최적 확장
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (w > 0 && h > 0) Math.min(w, h) else Math.max(w, h)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 28f
        val width = width.toFloat()
        val height = height.toFloat()

        // 1. 연한 베이지 바둑판 배경 그리기
        val boardRect = RectF(0f, 0f, width, height)
        canvas.drawRoundRect(boardRect, 16f, 16f, bgPaint)

        val cellWidth = (width - padding * 2) / (boardSize - 1)
        val cellHeight = (height - padding * 2) / (boardSize - 1)

        // 2. 17x17 격자선 그리기 (갈색-검정 사이 색상)
        for (i in 0 until boardSize) {
            // 가로선
            val y = padding + i * cellHeight
            canvas.drawLine(padding, y, width - padding, y, linePaint)

            // 세로선
            val x = padding + i * cellWidth
            canvas.drawLine(x, padding, x, height - padding, linePaint)
        }

        // 3. 17x17 오목판 화점(점) 그리기 (3,3), (3,13), (13,3), (13,13), (8,8)
        val starPoints = listOf(Pair(3, 3), Pair(3, 13), Pair(13, 3), Pair(13, 13), Pair(8, 8))
        for (pt in starPoints) {
            val sx = padding + pt.second * cellWidth
            val sy = padding + pt.first * cellHeight
            canvas.drawCircle(sx, sy, 5.5f, starPaint)
        }

        // 4. 놓여진 오목 돌(흑돌/백돌) 그리기
        val stoneRadius = cellWidth * 0.44f
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val stoneType = boardState[r][c]
                if (stoneType != 0) {
                    val cx = padding + c * cellWidth
                    val cy = padding + r * cellHeight

                    if (stoneType == 1) { // 흑돌
                        canvas.drawCircle(cx, cy, stoneRadius, blackStonePaint)
                        canvas.drawCircle(cx, cy, stoneRadius, blackStoneStroke)
                    } else if (stoneType == 2) { // 백돌
                        canvas.drawCircle(cx, cy, stoneRadius, whiteStonePaint)
                        canvas.drawCircle(cx, cy, stoneRadius, whiteStoneStroke)
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !isGameOver && isUserTurn) {
            val padding = 28f
            val cellWidth = (width - padding * 2) / (boardSize - 1)
            val cellHeight = (height - padding * 2) / (boardSize - 1)

            val col = Math.round((event.x - padding) / cellWidth)
            val row = Math.round((event.y - padding) / cellHeight)

            if (row in 0 until boardSize && col in 0 until boardSize) {
                if (boardState[row][col] == 0) {
                    // 1. 내 돌(흑돌) 착수
                    boardState[row][col] = 1
                    invalidate()

                    if (checkFiveInARow(row, col, 1)) {
                        isGameOver = true
                        onTurnChangeListener?.invoke(false, "🎉 축하합니다! 내 차(User) 승리!")
                        return true
                    }

                    // 2. 상대방 차례로 넘기기 및 AI 자동 착수 (1초 후)
                    isUserTurn = false
                    onTurnChangeListener?.invoke(false, "⏳ 상대방이 신중하게 생각 중입니다...")

                    postDelayed({
                        makeOpponentMove()
                    }, 1000)
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun makeOpponentMove() {
        if (isGameOver) return

        // 상대방(백돌) 승리 수 / 방어 수 / 랜덤 수 탐색
        var bestRow = -1
        var bestCol = -1

        // 1. 백돌 5목 완성 수
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                if (boardState[r][c] == 0) {
                    boardState[r][c] = 2
                    if (checkFiveInARow(r, c, 2)) {
                        bestRow = r
                        bestCol = c
                    }
                    boardState[r][c] = 0
                }
            }
        }

        // 2. 흑돌(사용자) 4목 차단 방어 수
        if (bestRow == -1) {
            for (r in 0 until boardSize) {
                for (c in 0 until boardSize) {
                    if (boardState[r][c] == 0) {
                        boardState[r][c] = 1
                        if (checkFiveInARow(r, c, 1)) {
                            bestRow = r
                            bestCol = c
                        }
                        boardState[r][c] = 0
                    }
                }
            }
        }

        // 3. 인접 빈 칸 랜덤 착수
        if (bestRow == -1) {
            val emptyList = mutableListOf<Pair<Int, Int>>()
            for (r in 0 until boardSize) {
                for (c in 0 until boardSize) {
                    if (boardState[r][c] == 0 && hasNeighbor(r, c)) {
                        emptyList.add(Pair(r, c))
                    }
                }
            }
            if (emptyList.isNotEmpty()) {
                val pick = emptyList.random()
                bestRow = pick.first
                bestCol = pick.second
            } else {
                bestRow = boardSize / 2
                bestCol = boardSize / 2
            }
        }

        if (bestRow != -1 && bestCol != -1) {
            boardState[bestRow][bestCol] = 2
            invalidate()

            if (checkFiveInARow(bestRow, bestCol, 2)) {
                isGameOver = true
                onTurnChangeListener?.invoke(true, "💀 상대방의 승리! 다음 기회에...")
            } else {
                isUserTurn = true
                onTurnChangeListener?.invoke(true, "🟢 내 차례입니다. 바둑판을 터치하세요!")
            }
        }
    }

    private fun hasNeighbor(r: Int, c: Int): Boolean {
        for (dr in -2..2) {
            for (dc in -2..2) {
                if (dr == 0 && dc == 0) continue
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until boardSize && nc in 0 until boardSize) {
                    if (boardState[nr][nc] != 0) return true
                }
            }
        }
        return false
    }

    private fun checkFiveInARow(r: Int, c: Int, player: Int): Boolean {
        val directions = listOf(
            Pair(0, 1),  // 가로
            Pair(1, 0),  // 세로
            Pair(1, 1),  // 우하 대각선
            Pair(1, -1)  // 좌하 대각선
        )

        for (dir in directions) {
            var count = 1

            // 정방향
            var step = 1
            while (true) {
                val nr = r + dir.first * step
                val nc = c + dir.second * step
                if (nr in 0 until boardSize && nc in 0 until boardSize && boardState[nr][nc] == player) {
                    count++
                    step++
                } else break
            }

            // 역방향
            step = 1
            while (true) {
                val nr = r - dir.first * step
                val nc = c - dir.second * step
                if (nr in 0 until boardSize && nc in 0 until boardSize && boardState[nr][nc] == player) {
                    count++
                    step++
                } else break
            }

            if (count >= 5) return true
        }
        return false
    }
}