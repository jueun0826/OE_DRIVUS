package com.example.oe

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

data class VisitedNode(
    val title: String,
    val date: String,
    val distance: String,
    val relX: Float, // 0.0f ~ 1.0f (Left ~ Right)
    val relY: Float  // 0.0f ~ 1.0f (Top/North ~ Bottom/South)
)

class TimelinePathView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var friendName: String = "김플레오"
        set(value) {
            field = value
            updateNodesForFriend(value)
            invalidate()
        }

    var visitedNodes: List<VisitedNode> = emptyList()

    // 지도 연한 백그라운드 Paint
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFF8FAFC.toInt()
        style = Paint.Style.FILL
    }

    // 도로 네트워크 선 Paint
    private val roadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE2E8F0.toInt()
        strokeWidth = 10f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // 1달간 이동한 주요 경로 Polyline Paint (파란색 선)
    private val polylinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2563EB.toInt()
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    // 점선 경로 Paint
    private val dashLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3B82F6.toInt()
        strokeWidth = 5f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(12f, 10f), 0f)
    }

    // 핀 노드 / 위치 포인트 Paint
    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2563EB.toInt()
        style = Paint.Style.FILL
    }

    private val nodeInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }

    private val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
        setShadowLayer(4f, 0f, 2f, 0x1A000000.toInt())
    }

    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0F172A.toInt()
        isFakeBoldText = true
    }

    private val dateTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF64748B.toInt()
    }

    private val routePath = Path()

    init {
        updateNodesForFriend(friendName)
    }

    fun updateNodesForFriend(name: String) {
        visitedNodes = when {
            name.contains("이현대") -> listOf(
                VisitedNode("🚩 10/03 광화문", "10/03", "12.0 km 주행", 0.20f, 0.48f),
                VisitedNode("🚩 10/10 천호역", "10/10", "23.5 km 주행", 0.46f, 0.55f),
                VisitedNode("🚩 10/18 속초해수욕장", "10/18", "185.0 km 주행", 0.82f, 0.22f),
                VisitedNode("🚩 10/27 강릉 경포대", "10/27", "210.0 km 주행", 0.78f, 0.76f)
            )
            name.contains("성주은") -> listOf(
                VisitedNode("🚩 10/02 북악스카이웨이", "10/02", "14.8 km 주행", 0.42f, 0.18f),
                VisitedNode("🚩 10/11 여의도 한강공원", "10/11", "21.0 km 주행", 0.22f, 0.48f),
                VisitedNode("🚩 10/19 판교 테크노밸리", "10/19", "38.5 km 주행", 0.58f, 0.78f),
                VisitedNode("🚩 10/28 용인 한국민속촌", "10/28", "45.2 km 주행", 0.80f, 0.72f)
            )
            name.contains("이예온") -> listOf(
                VisitedNode("🚩 10/04 잠실 롯데월드몰", "10/04", "19.4 km 주행", 0.25f, 0.76f),
                VisitedNode("🚩 10/12 남양주 자전거길", "10/12", "42.0 km 주행", 0.50f, 0.50f),
                VisitedNode("🚩 10/22 가평 글램핑장", "10/22", "78.5 km 주행", 0.78f, 0.26f),
                VisitedNode("🚩 10/29 춘천 의암호", "10/29", "95.0 km 주행", 0.86f, 0.15f)
            )
            name.contains("김국민") -> listOf(
                VisitedNode("🚩 10/06 국민대 산학협력관", "10/06", "28.0 km 주행", 0.30f, 0.18f),
                VisitedNode("🚩 10/15 과천 중앙공원", "10/15", "22.4 km 주행", 0.48f, 0.50f),
                VisitedNode("🚩 10/21 의왕 현대위아연구소", "10/21", "26.0 km 주행", 0.35f, 0.78f),
                VisitedNode("🚩 10/28 수원 화성행궁", "10/28", "41.2 km 주행", 0.75f, 0.82f)
            )
            name.contains("김플레오맘") -> listOf(
                VisitedNode("🚩 10/04 은평 한옥마을", "10/04", "16.2 km 주행", 0.40f, 0.22f),
                VisitedNode("🚩 10/11 일산 호수공원", "10/11", "28.5 km 주행", 0.18f, 0.38f),
                VisitedNode("🚩 10/19 상암 MBC 미디어센터", "10/19", "12.4 km 주행", 0.35f, 0.55f),
                VisitedNode("🚩 10/26 영등포 타임스퀘어", "10/26", "21.0 km 주행", 0.48f, 0.78f)
            )
            name.contains("엄성현") -> listOf(
                VisitedNode("🚩 10/03 성수동 서울숲", "10/03", "14.2 km 주행", 0.42f, 0.28f),
                VisitedNode("🚩 10/12 하남 스타필드", "10/12", "31.0 km 주행", 0.78f, 0.42f),
                VisitedNode("🚩 10/20 분당 율동공원", "10/20", "26.8 km 주행", 0.52f, 0.75f),
                VisitedNode("🚩 10/27 광주 남한산성", "10/27", "34.5 km 주행", 0.82f, 0.68f)
            )
            name.contains("최자두") -> listOf(
                VisitedNode("🚩 10/05 인천 차이나타운", "10/05", "42.0 km 주행", 0.15f, 0.35f),
                VisitedNode("🚩 10/14 송도 센트럴파크", "10/14", "38.5 km 주행", 0.22f, 0.65f),
                VisitedNode("🚩 10/21 시흥 오이도 빨간등대", "10/21", "46.2 km 주행", 0.45f, 0.78f),
                VisitedNode("🚩 10/28 안산 대부도 방아머리", "10/28", "58.0 km 주행", 0.72f, 0.82f)
            )
            name.contains("최제네시스") -> listOf(
                VisitedNode("🚩 10/01 성남 모란시장", "10/01", "18.6 km 주행", 0.45f, 0.25f),
                VisitedNode("🚩 10/09 용인 에버랜드", "10/09", "44.0 km 주행", 0.62f, 0.52f),
                VisitedNode("🚩 10/16 이천 도자기마을", "10/16", "62.5 km 주행", 0.85f, 0.72f),
                VisitedNode("🚩 10/23 안성 농field", "10/23", "75.0 km 주행", 0.55f, 0.85f)
            )
            name.contains("한아이오닉") -> listOf(
                VisitedNode("🚩 10/02 파주 헤이리 예술마을", "10/02", "36.4 km 주행", 0.18f, 0.18f),
                VisitedNode("🚩 10/08 김포 아울렛", "10/08", "25.0 km 주행", 0.28f, 0.40f),
                VisitedNode("🚩 10/15 마곡 서울식물원", "10/15", "19.8 km 주행", 0.45f, 0.58f),
                VisitedNode("🚩 10/22 목동 현대백화점", "10/22", "14.5 km 주행", 0.60f, 0.78f)
            )
            else -> listOf( // 김플레오
                VisitedNode("🚩 10/05 국민대학교", "10/05", "24.6 km 주행", 0.35f, 0.20f),
                VisitedNode("🚩 10/12 광화문 광장", "10/12", "15.3 km 주행", 0.25f, 0.45f),
                VisitedNode("🚩 10/20 성수동", "10/20", "18.2 km 주행", 0.65f, 0.55f),
                VisitedNode("🚩 10/28 강남역", "10/28", "32.4 km 주행", 0.75f, 0.80f)
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        if (w <= 0 || h <= 0 || visitedNodes.isEmpty()) return

        // 1. 전체 배경 칠하기
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // 2. 격자/도로 네트워크 가이드 배경선 그리기
        canvas.drawLine(w * 0.1f, h * 0.2f, w * 0.9f, h * 0.2f, roadPaint)
        canvas.drawLine(w * 0.2f, h * 0.5f, w * 0.8f, h * 0.5f, roadPaint)
        canvas.drawLine(w * 0.1f, h * 0.8f, w * 0.9f, h * 0.8f, roadPaint)
        canvas.drawLine(w * 0.3f, h * 0.1f, w * 0.3f, h * 0.9f, roadPaint)
        canvas.drawLine(w * 0.7f, h * 0.1f, w * 0.7f, h * 0.9f, roadPaint)

        // 3. 주행 경로 (Polyline 곡선으로 실제 위치 노드 연결)
        routePath.reset()
        val first = visitedNodes[0]
        routePath.moveTo(first.relX * w, first.relY * h)

        for (i in 1 until visitedNodes.size) {
            val prev = visitedNodes[i - 1]
            val curr = visitedNodes[i]

            val pX = prev.relX * w
            val pY = prev.relY * h
            val cX = curr.relX * w
            val cY = curr.relY * h

            val ctrl1X = (pX + cX) / 2f
            val ctrl1Y = pY
            val ctrl2X = (pX + cX) / 2f
            val ctrl2Y = cY

            routePath.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, cX, cY)
        }

        canvas.drawPath(routePath, polylinePaint)
        canvas.drawPath(routePath, dashLinePaint)

        // 4. 각 방문 노드 포인트 및 라벨 카드 그리기 (20sp 기준)
        for (node in visitedNodes) {
            drawTimelineNode(canvas, node.relX * w, node.relY * h, node.title, node.distance)
        }
    }

    private fun drawTimelineNode(
        canvas: Canvas,
        x: Float,
        y: Float,
        title: String,
        sub: String
    ) {
        val density = context.resources.displayMetrics.density
        val titleTextSize = 18f * density
        val subTextSize = 13f * density

        labelTextPaint.textSize = titleTextSize
        dateTextPaint.textSize = subTextSize

        // 노드 동그라미
        canvas.drawCircle(x, y, 8f * density, nodePaint)
        canvas.drawCircle(x, y, 3f * density, nodeInnerPaint)

        // 라벨 박스 크기 조절
        val titleWidth = labelTextPaint.measureText(title)
        val subWidth = dateTextPaint.measureText(sub)
        val maxTextWidth = Math.max(titleWidth, subWidth)

        val paddingH = 10f * density
        val paddingV = 6f * density
        val boxWidth = maxTextWidth + (paddingH * 2)
        val boxHeight = titleTextSize + subTextSize + (paddingV * 2.2f)

        var boxLeft = x + 14f
        val boxTop = y - (boxHeight / 2f)

        if (boxLeft + boxWidth > width - 10f) {
            boxLeft = x - boxWidth - 14f
        }
        if (boxLeft < 10f) {
            boxLeft = 10f
        }

        val rect = RectF(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight)
        canvas.drawRoundRect(rect, 8f * density, 8f * density, labelBgPaint)

        val textX = boxLeft + paddingH
        val titleY = boxTop + paddingV + titleTextSize * 0.78f
        val subY = titleY + subTextSize + 4f * density

        canvas.drawText(title, textX, titleY, labelTextPaint)
        canvas.drawText(sub, textX, subY, dateTextPaint)
    }
}