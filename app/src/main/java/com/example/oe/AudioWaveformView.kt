package com.example.oe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class AudioWaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFEF4444.toInt() // 붉은색 오디오 파형 (레퍼런스 이미지 매칭)
        strokeWidth = 6f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val centerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFEF4444.toInt()
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFEF4444.toInt()
        style = Paint.Style.FILL
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1E1E1E.toInt() // 다크 블랙 웨이브폼 카드 배경
        style = Paint.Style.FILL
    }

    private val amplitudes = ArrayList<Float>()
    private val maxBars = 70
    private var isRecording = false

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                // 실시간 마이크 데시벨 오디오 파형 난수 생성
                val nextAmp = Random.nextFloat() * 0.8f + 0.15f
                amplitudes.add(nextAmp)
                if (amplitudes.size > maxBars) {
                    amplitudes.removeAt(0)
                }
                invalidate()
                handler.postDelayed(this, 70)
            }
        }
    }

    fun startAnimation() {
        isRecording = true
        amplitudes.clear()
        // 초기 가상 파형 채우기
        for (i in 0..40) {
            amplitudes.add(Random.nextFloat() * 0.5f + 0.1f)
        }
        handler.removeCallbacks(updateRunnable)
        handler.post(updateRunnable)
    }

    fun stopAnimation() {
        isRecording = false
        handler.removeCallbacks(updateRunnable)
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        if (w <= 0 || h <= 0) return

        // 배경 그리기
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // 중심선 기준 수평 가이드선
        val centerY = h / 2f
        val barSpacing = 12f

        // 파형 바들 그리기
        val startX = w - (amplitudes.size * barSpacing) - 20f

        for (i in amplitudes.indices) {
            val barX = startX + (i * barSpacing)
            if (barX in 0f..w) {
                val barHeight = (amplitudes[i] * h * 0.75f).coerceAtLeast(8f)
                val topY = centerY - (barHeight / 2f)
                val bottomY = centerY + (barHeight / 2f)
                canvas.drawLine(barX, topY, barX, bottomY, wavePaint)
            }
        }

        // 중앙 빨간색 녹음 재생헤드 세로 라인 & 상하 도트 (레퍼런스 이미지 연동)
        val playheadX = w * 0.65f
        canvas.drawLine(playheadX, 0f, playheadX, h, centerLinePaint)
        canvas.drawCircle(playheadX, 8f, 6f, centerDotPaint)
        canvas.drawCircle(playheadX, h - 8f, 6f, centerDotPaint)
    }
}