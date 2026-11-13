package com.example.oe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class PleosNavSimulationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var friendName: String = "성주은"
    var avatarEmoji: String = "⚡"
    var startLocName: String = "동서울터미널"
    var destLocName: String = "속초해수욕장"
    var speedText: String = "102 km/h"
    var isDriving: Boolean = true

    // Pleos FusedLocation 실시간 좌표 데이터
    var currentLat: Double = 37.4979
    var currentLng: Double = 127.0276
    var isFusedLocationActive: Boolean = false
    var locationStatusText: String = "FusedLocation 정밀 수신 중 📡"

    // 실시간 위치 진행율 (0.0f ~ 1.0f)
    private var progressRatio = 0.65f
    private var pulseRadius = 0f
    private var isPulsingUp = true

    // 순정 네비 라이트 모드 색상 (예시 이미지와 100% 매칭)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFF1F5F9.toInt() // 예시 이미지와 동일한 라이트 실버/베이지 맵 배경
        style = Paint.Style.FILL
    }

    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE2E8F0.toInt()
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    // 교통 상태별 멀티 컬러 라인 (파랑-주황-빨강-노랑)
    private val routeBluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3B82F6.toInt()
        strokeWidth = 18f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val routeOrangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFF59E0B.toInt()
        strokeWidth = 18f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val routeRedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFEF4444.toInt()
        strokeWidth = 18f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val routeGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x603B82F6.toInt()
        style = Paint.Style.FILL
    }

    // 좌측 상단 블랙 HUD 박스
    private val blackHudBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF18181B.toInt()
        style = Paint.Style.FILL
    }

    private val whiteTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 28f
        isFakeBoldText = true
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFA1A1AA.toInt()
        textSize = 18f
    }

    // 하단 ETA 화이트 카드
    private val whiteCardBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }

    private val darkTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0F172A.toInt()
        textSize = 22f
        isFakeBoldText = true
    }

    private val gpsCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2563EB.toInt()
        style = Paint.Style.FILL
    }

    private val gpsArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 24f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    init {
        post(object : Runnable {
            override fun run() {
                if (isDriving) {
                    progressRatio += 0.0015f
                    if (progressRatio > 0.9f) progressRatio = 0.2f

                    // FusedLocation 미세 좌표 업데이트 시뮬레이션
                    currentLat = 37.4979 + (progressRatio - 0.65) * 0.005
                    currentLng = 127.0276 + (progressRatio - 0.65) * 0.005

                    if (isPulsingUp) {
                        pulseRadius += 0.8f
                        if (pulseRadius > 20f) isPulsingUp = false
                    } else {
                        pulseRadius -= 0.8f
                        if (pulseRadius < 6f) isPulsingUp = true
                    }
                    invalidate()
                }
                postDelayed(this, 50)
            }
        })
    }

    // FusedLocation SDK 실시간 좌표 수신 매핑 함수
    fun updateRealtimeFusedLocation(lat: Double, lng: Double, speed: Float) {
        this.currentLat = lat
        this.currentLng = lng
        this.speedText = "${speed.toInt()} km/h"
        this.isFusedLocationActive = true
        this.locationStatusText = "FusedLocation 정밀 수신 중 📡"
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // 1. 네비 라이트 배경그리기
        val bgRect = RectF(0f, 0f, w, h)
        canvas.drawRoundRect(bgRect, 16f, 16f, bgPaint)

        // 2. 배경 그리드
        var x = 0f
        while (x < w) {
            canvas.drawLine(x, 0f, x, h, gridLinePaint)
            x += 70f
        }
        var y = 0f
        while (y < h) {
            canvas.drawLine(0f, y, w, y, gridLinePaint)
            y += 70f
        }

        // 3. 예시 이미지와 100% 동일한 S자 다색 교통 경로선 (블루 - 주황 - 빨강 - 주황 - 파랑)
        val pathSegment1 = Path().apply {
            moveTo(w * 0.05f, h * 0.70f)
            lineTo(w * 0.38f, h * 0.32f)
        }
        val pathSegment2 = Path().apply {
            moveTo(w * 0.38f, h * 0.32f)
            cubicTo(w * 0.42f, h * 0.12f, w * 0.65f, h * 0.10f, w * 0.70f, h * 0.28f)
        }
        val pathSegment3 = Path().apply {
            moveTo(w * 0.70f, h * 0.28f)
            cubicTo(w * 0.75f, h * 0.45f, w * 0.82f, h * 0.58f, w * 0.60f, h * 0.68f)
        }
        val pathSegment4 = Path().apply {
            moveTo(w * 0.60f, h * 0.68f)
            lineTo(w * 0.40f, h * 0.75f)
        }

        canvas.drawPath(pathSegment1, routeBluePaint)
        canvas.drawPath(pathSegment2, routeOrangePaint)
        canvas.drawPath(pathSegment3, routeRedPaint)
        canvas.drawPath(pathSegment4, routeOrangePaint)

        // 4. 파란색 GPS 화살표 포인터 아이콘 (실시간 좌표에 따라 동적 이동)
        val gpsX = w * (0.10f + progressRatio * 0.75f)
        val gpsY = h * (0.75f - progressRatio * 0.60f)

        canvas.drawCircle(gpsX, gpsY, 22f + pulseRadius * 0.5f, routeGlowPaint)
        canvas.drawCircle(gpsX, gpsY, 20f, gpsCirclePaint)
        canvas.drawText("▲", gpsX, gpsY + 8f, gpsArrowPaint)

        // 5. [좌측 상단] 예시 이미지와 100% 동일한 블랙 Turn-by-Turn HUD 오버레이
        val hudRect = RectF(16f, 16f, 440f, 120f)
        canvas.drawRoundRect(hudRect, 12f, 12f, blackHudBg)

        // 턴 화살표 & 거리
        whiteTextPaint.textSize = 28f
        canvas.drawText("↗️ 700m", 32f, 58f, whiteTextPaint)
        subTextPaint.color = 0xFFA1A1AA.toInt()
        subTextPaint.textSize = 16f
        canvas.drawText("대왕판교IC 방면", 32f, 92f, subTextPaint)

        // FusedLocation 정밀 수신 실시간 좌표 텍스트 표출
        val formattedLat = String.format(java.util.Locale.US, "%.4f", currentLat)
        val formattedLng = String.format(java.util.Locale.US, "%.4f", currentLng)
        val subHudRect = RectF(16f, 126f, 440f, 172f)
        canvas.drawRoundRect(subHudRect, 8f, 8f, blackHudBg)
        whiteTextPaint.textSize = 16f
        canvas.drawText("📍 N ${formattedLat}°, E ${formattedLng}° (FusedLocation)", 32f, 156f, whiteTextPaint)

        // 6. [우측 상단] 2D 나침반 배지 (예시 이미지와 동일)
        val compassRect = RectF(w - 90f, 16f, w - 16f, 80f)
        canvas.drawRoundRect(compassRect, 8f, 8f, whiteCardBg)
        darkTextPaint.textSize = 20f
        canvas.drawText("2D ▲", w - 72f, 54f, darkTextPaint)

        // 7. [좌측 하단] 예시 이미지와 100% 동일한 ETA & 배터리 정보 카드
        val etaRect = RectF(16f, h - 140f, 380f, h - 16f)
        canvas.drawRoundRect(etaRect, 12f, 12f, whiteCardBg)

        darkTextPaint.textSize = 26f
        canvas.drawText("11:14 PM", 32f, h - 100f, darkTextPaint)

        subTextPaint.color = 0xFF64748B.toInt()
        subTextPaint.textSize = 18f
        canvas.drawText("46분 · 32km", 230f, h - 100f, subTextPaint)

        subTextPaint.color = 0xFF0F172A.toInt()
        subTextPaint.textSize = 18f
        canvas.drawText("📍 $destLocName", 32f, h - 65f, subTextPaint)

        subTextPaint.color = 0xFF059669.toInt()
        subTextPaint.textSize = 16f
        canvas.drawText("🔋 94% (잔여거리 여유)", 200f, h - 65f, subTextPaint)

        // 진행 프로그레스 바
        val pbBg = RectF(32f, h - 45f, 360f, h - 37f)
        canvas.drawRoundRect(pbBg, 4f, 4f, gridLinePaint)
        val pbFill = RectF(32f, h - 45f, 32f + 328f * progressRatio, h - 37f)
        canvas.drawRoundRect(pbFill, 4f, 4f, routeBluePaint)
    }
}