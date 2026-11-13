package com.example.oe

import android.app.Dialog
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.ErrorType
import ai.pleos.playground.stt.listener.OnInitializeListener
import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.DoorArea
import ai.pleos.playground.vehicle.constant.area.DoorValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var vehicle: Vehicle
    private lateinit var speechToText: SpeechToText
    private lateinit var naviHelper: NaviHelper
    private lateinit var db: FirebaseFirestore

    private lateinit var tvDoorStatus: TextView
    private lateinit var btnRefreshVehicle: Button

    // 내 SNS 프로필 뷰
    private lateinit var tvMyAvatarEmoji: TextView
    private lateinit var tvMyProfileHandle: TextView
    private lateinit var tvMyProfileBio: TextView
    private lateinit var btnEditMyProfile: Button

    // 10명 친구 레이아웃 & 아바타 & 버튼
    private lateinit var layoutFriend1: View
    private lateinit var layoutFriend2: View
    private lateinit var layoutFriend3: View
    private lateinit var layoutFriend4: View
    private lateinit var layoutFriend5: View
    private lateinit var layoutFriend6: View
    private lateinit var layoutFriend7: View
    private lateinit var layoutFriend8: View
    private lateinit var layoutFriend9: View
    private lateinit var layoutFriend10: View

    private lateinit var avatarFriend1: View
    private lateinit var avatarFriend2: View
    private lateinit var avatarFriend3: View
    private lateinit var avatarFriend4: View
    private lateinit var avatarFriend5: View
    private lateinit var avatarFriend6: View
    private lateinit var avatarFriend7: View
    private lateinit var avatarFriend8: View
    private lateinit var avatarFriend9: View
    private lateinit var avatarFriend10: View

    private lateinit var btnPokeFriend1: Button
    private lateinit var btnPokeFriend2: Button
    private lateinit var btnPokeFriend3: Button
    private lateinit var btnPokeFriend4: Button
    private lateinit var btnPokeFriend5: Button
    private lateinit var btnPokeFriend6: Button
    private lateinit var btnPokeFriend7: Button
    private lateinit var btnPokeFriend8: Button
    private lateinit var btnPokeFriend9: Button
    private lateinit var btnPokeFriend10: Button

    private lateinit var btnVoiceFriend1: Button
    private lateinit var btnVoiceFriend2: Button
    private lateinit var btnVoiceFriend3: Button
    private lateinit var btnVoiceFriend4: Button
    private lateinit var btnVoiceFriend5: Button
    private lateinit var btnVoiceFriend6: Button
    private lateinit var btnVoiceFriend7: Button
    private lateinit var btnVoiceFriend8: Button
    private lateinit var btnVoiceFriend9: Button
    private lateinit var btnVoiceFriend10: Button

    // 4개 기능 카드 (2x2 그리드)
    private lateinit var cardLeaderboard: View
    private lateinit var cardGames: View
    private lateinit var cardChat: View
    private lateinit var cardSettings: View

    private var currentDoorPositionVal: Int? = null
    private var liveChatHistoryString: String = "🟢 [실시간 채팅 & 콕찌르기 대화 내역]\n\n"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // SpeechToText SDK 객체 생성 및 초기화
        speechToText = SpeechToText(this)
        speechToText.initialize(object : OnInitializeListener {
            override fun onSuccess() {
                Log.d("STT", "SpeechToText SDK 초기화 성공! 음성 서비스 준비 완료")
            }

            override fun onFailure(errorType: ErrorType) {
                Log.e("STT", "SpeechToText SDK 초기화 실패: $errorType")
            }
        })

        // NaviHelper SDK 객체 생성 및 초기화
        naviHelper = NaviHelper(applicationContext)
        naviHelper.initialize(
            onConnected = {
                Log.d("NaviHelper", "NaviHelper SDK 서비스 연결 성공! 네비게이션 기능 수신 가능")
            },
            onFailed = { throwable ->
                Log.e("NaviHelper", "NaviHelper SDK 서비스 연결 실패: ${throwable.message}")
            }
        )

        // UI 뷰 연결
        tvDoorStatus = findViewById(R.id.tvDoorStatus)
        btnRefreshVehicle = findViewById(R.id.btnRefreshVehicle)

        // 내 SNS 프로필 뷰 연결
        tvMyAvatarEmoji = findViewById(R.id.tvMyAvatarEmoji)
        tvMyProfileHandle = findViewById(R.id.tvMyProfileHandle)
        tvMyProfileBio = findViewById(R.id.tvMyProfileBio)
        btnEditMyProfile = findViewById(R.id.btnEditMyProfile)

        // 10명 친구 뷰 연결
        layoutFriend1 = findViewById(R.id.layoutFriend1)
        layoutFriend2 = findViewById(R.id.layoutFriend2)
        layoutFriend3 = findViewById(R.id.layoutFriend3)
        layoutFriend4 = findViewById(R.id.layoutFriend4)
        layoutFriend5 = findViewById(R.id.layoutFriend5)
        layoutFriend6 = findViewById(R.id.layoutFriend6)
        layoutFriend7 = findViewById(R.id.layoutFriend7)
        layoutFriend8 = findViewById(R.id.layoutFriend8)
        layoutFriend9 = findViewById(R.id.layoutFriend9)
        layoutFriend10 = findViewById(R.id.layoutFriend10)

        avatarFriend1 = findViewById(R.id.avatarFriend1)
        avatarFriend2 = findViewById(R.id.avatarFriend2)
        avatarFriend3 = findViewById(R.id.avatarFriend3)
        avatarFriend4 = findViewById(R.id.avatarFriend4)
        avatarFriend5 = findViewById(R.id.avatarFriend5)
        avatarFriend6 = findViewById(R.id.avatarFriend6)
        avatarFriend7 = findViewById(R.id.avatarFriend7)
        avatarFriend8 = findViewById(R.id.avatarFriend8)
        avatarFriend9 = findViewById(R.id.avatarFriend9)
        avatarFriend10 = findViewById(R.id.avatarFriend10)

        btnPokeFriend1 = findViewById(R.id.btnPokeFriend1)
        btnPokeFriend2 = findViewById(R.id.btnPokeFriend2)
        btnPokeFriend3 = findViewById(R.id.btnPokeFriend3)
        btnPokeFriend4 = findViewById(R.id.btnPokeFriend4)
        btnPokeFriend5 = findViewById(R.id.btnPokeFriend5)
        btnPokeFriend6 = findViewById(R.id.btnPokeFriend6)
        btnPokeFriend7 = findViewById(R.id.btnPokeFriend7)
        btnPokeFriend8 = findViewById(R.id.btnPokeFriend8)
        btnPokeFriend9 = findViewById(R.id.btnPokeFriend9)
        btnPokeFriend10 = findViewById(R.id.btnPokeFriend10)

        btnVoiceFriend1 = findViewById(R.id.btnVoiceFriend1)
        btnVoiceFriend2 = findViewById(R.id.btnVoiceFriend2)
        btnVoiceFriend3 = findViewById(R.id.btnVoiceFriend3)
        btnVoiceFriend4 = findViewById(R.id.btnVoiceFriend4)
        btnVoiceFriend5 = findViewById(R.id.btnVoiceFriend5)
        btnVoiceFriend6 = findViewById(R.id.btnVoiceFriend6)
        btnVoiceFriend7 = findViewById(R.id.btnVoiceFriend7)
        btnVoiceFriend8 = findViewById(R.id.btnVoiceFriend8)
        btnVoiceFriend9 = findViewById(R.id.btnVoiceFriend9)
        btnVoiceFriend10 = findViewById(R.id.btnVoiceFriend10)

        // 4개 기능 카드 뷰 연결 (2x2 그리드)
        cardLeaderboard = findViewById(R.id.cardLeaderboard)
        cardGames = findViewById(R.id.cardGames)
        cardChat = findViewById(R.id.cardChat)
        cardSettings = findViewById(R.id.cardSettings)

        // 1. Firebase Firestore 초기화
        db = FirebaseFirestore.getInstance()

        // 2. Pleos Vehicle SDK 초기화
        vehicle = Vehicle(this)
        vehicle.initialize()

        // 위치 권한 요청 및 수신
        requestLocationPermission()

        // 3. 차량 상태 조회
        fetchVehicleDoorStatus()

        // 4. 리스너 & 버튼 연결
        btnRefreshVehicle.setOnClickListener {
            fetchVehicleDoorStatus()
            fetchCurrentLocation()
        }

        // [내 프로필 수정 버튼 클릭 이벤트]
        btnEditMyProfile.setOnClickListener {
            showEditMyProfileDialog()
        }

        // [10명 친구 바 클릭 시 - 통합 단일 탭 팝업 (사용자가 변경한 이름 및 한줄소개 매칭)]
        val openKimPleo = View.OnClickListener {
            showUnifiedFriendDialog("김플레오", "pleo_kim", "👨‍✈️", "오늘도 안운! 강남역 만날 사람~~ ", true, false, "판교테크노밸리", "강남역 (서울특별시 강남구 강남대로 396)", "N 37.4979°, E 127.0276° (경부고속도로 하행선)", "65 km/h", "14.2 km / 약 22분 예상", "IONIQ 5 / 문 닫힘 / 안전벨트 착용 완료")
        }
        avatarFriend1.setOnClickListener(openKimPleo)
        layoutFriend1.setOnClickListener(openKimPleo)

        val openLeeHyundai = View.OnClickListener {
            showUnifiedFriendDialog("이현대", "hyundai_lee", "🏎️", "출근중ㅠㅠ오늘도 파이팅~!! ", true, false, "여의도", "인천국제공항 제1여객터미널", "N 37.4485°, E 126.6853° (인천대교 고속도로)", "88 km/h", "45.0 km / 약 38분 예상", "EV6 / 도어 닫힘 / 크루즈 컨트롤 작동 중")
        }
        avatarFriend3.setOnClickListener(openLeeHyundai)
        layoutFriend3.setOnClickListener(openLeeHyundai)

        val openSeongJueun = View.OnClickListener {
            showUnifiedFriendDialog("성주은", "jueun_seong", "⚡", "벌써 가을이네~~!! ", true, false, "동서울터미널", "속초해수욕장", "N 37.8102°, E 128.1205° (서울양양고속도로)", "102 km/h", "82.0 km / 약 55분 예상", "IONIQ 6 / 스포츠 모드 운행")
        }
        avatarFriend5.setOnClickListener(openSeongJueun)
        layoutFriend5.setOnClickListener(openSeongJueun)

        val openLeeYeoon = View.OnClickListener {
            showUnifiedFriendDialog("이예온", "yeoon_lee", "🚘", "가평 글램핑장 가는 중ㅎㅎ ", true, false, "하남스타필드", "가평 산솔캠핑장", "N 37.8201°, E 127.5100° (북한강로 지방도)", "45 km/h", "8.5 km / 약 12분 예상", "EV9 6인승 / 루프박스 장착 완료")
        }
        avatarFriend6.setOnClickListener(openLeeYeoon)
        layoutFriend6.setOnClickListener(openLeeYeoon)

        val openKimKookmin = View.OnClickListener {
            showUnifiedFriendDialog("김국민", "kookmin_kim", "🌄", "동해안 해안도로 드라이브 중ㅋㅋ!! ", true, false, "강릉역", "정동진 해변", "N 37.6910°, E 129.0321° (헌화로)", "55 km/h", "25.0 km / 약 30분 예상", "토레스 EVX / 드라이브 가동 중")
        }
        avatarFriend9.setOnClickListener(openKimKookmin)
        layoutFriend9.setOnClickListener(openKimKookmin)

        val openPleoMom = View.OnClickListener {
            showUnifiedFriendDialog("김플레오맘", "pleo_mom", "👤", "플레오맘. 이에요~^^ ", false, false, "정자역", "현대백화점 판교점 지상 주차장", "N 37.3925°, E 127.1120° (판교역 로터리)", "0 km/h (정차 중)", "도착 완료 (P기어)", "GV80 / 운전석 도어 열림 🚪")
        }
        avatarFriend2.setOnClickListener(openPleoMom)
        layoutFriend2.setOnClickListener(openPleoMom)

        val openSunghyunEom = View.OnClickListener {
            showUnifiedFriendDialog("엄성현", "sunghyun_eom", "👔", "드라이브 스루 주문 완 ", false, false, "수내역", "스타벅스 DT 정자점", "N 37.3688°, E 127.1088° (정자일로)", "0 km/h (정차 중)", "주문 대기 중", "K8 하이브리드 / 음료 수령 대기")
        }
        avatarFriend7.setOnClickListener(openSunghyunEom)
        layoutFriend7.setOnClickListener(openSunghyunEom)

        val openJaduChoi = View.OnClickListener {
            showUnifiedFriendDialog("최자두", "jadu_choi", "🐤", "새콤하게 자~달콤하게 두~", false, false, "서현역", "율동공원", "N 37.3780°, E 127.1400° (율동공원 로터리)", "0 km/h (정차 중)", "신호 대기 중", "캐스퍼 / 초보운전 스티커 부착")
        }
        avatarFriend10.setOnClickListener(openJaduChoi)
        layoutFriend10.setOnClickListener(openJaduChoi)

        val openChoiGenesis = View.OnClickListener {
            showUnifiedFriendDialog("최제네시스", "genesis_choi", "👤", "차 수리맡겼다...ㅠㅠ", false, true, "비운행", "차량 미운행 상태", "N 37.5665°, E 126.9780° (서울시청 부근)", "0 km/h (보행 중)", "-", "차량 탑승 안함 🚶")
        }
        avatarFriend4.setOnClickListener(openChoiGenesis)
        layoutFriend4.setOnClickListener(openChoiGenesis)

        val openIoniqHan = View.OnClickListener {
            showUnifiedFriendDialog("한아이오닉", "ioniq_han", "😎", "이번 주 세차 갈 사람~", false, true, "비운행", "차량 미운행 상태", "N 37.5100°, E 127.0500° (삼성동 부근)", "0 km/h (보행 중)", "-", "차량 탑승 안함 🚶")
        }
        avatarFriend8.setOnClickListener(openIoniqHan)
        layoutFriend8.setOnClickListener(openIoniqHan)

        // 10명 콕찌르기 버튼 연결 (Enabled -> Pressed -> Loading -> Disabled)
        btnPokeFriend1.setOnClickListener { sendPokeToFriend("김플레오", btnPokeFriend1) }
        btnPokeFriend2.setOnClickListener { sendPokeToFriend("김플레오맘", btnPokeFriend2) }
        btnPokeFriend3.setOnClickListener { sendPokeToFriend("이현대", btnPokeFriend3) }
        btnPokeFriend4.setOnClickListener { sendPokeToFriend("최제네시스", btnPokeFriend4) }
        btnPokeFriend5.setOnClickListener { sendPokeToFriend("성주은", btnPokeFriend5) }
        btnPokeFriend6.setOnClickListener { sendPokeToFriend("이예온", btnPokeFriend6) }
        btnPokeFriend7.setOnClickListener { sendPokeToFriend("엄성현", btnPokeFriend7) }
        btnPokeFriend8.setOnClickListener { sendPokeToFriend("한아이오닉", btnPokeFriend8) }
        btnPokeFriend9.setOnClickListener { sendPokeToFriend("김국민", btnPokeFriend9) }
        btnPokeFriend10.setOnClickListener { sendPokeToFriend("최자두", btnPokeFriend10) }

        // 10명 🎙️ 음성 메시지 선택 방식 팝업 연결 (Enabled -> Pressed)
        btnVoiceFriend1.setOnClickListener { showVoiceOptionSelectionDialog("김플레오") }
        btnVoiceFriend2.setOnClickListener { showVoiceOptionSelectionDialog("김플레오맘") }
        btnVoiceFriend3.setOnClickListener { showVoiceOptionSelectionDialog("이현대") }
        btnVoiceFriend4.setOnClickListener { showVoiceOptionSelectionDialog("최제네시스") }
        btnVoiceFriend5.setOnClickListener { showVoiceOptionSelectionDialog("성주은") }
        btnVoiceFriend6.setOnClickListener { showVoiceOptionSelectionDialog("이예온") }
        btnVoiceFriend7.setOnClickListener { showVoiceOptionSelectionDialog("엄성현") }
        btnVoiceFriend8.setOnClickListener { showVoiceOptionSelectionDialog("한아이오닉") }
        btnVoiceFriend9.setOnClickListener { showVoiceOptionSelectionDialog("김국민") }
        btnVoiceFriend10.setOnClickListener { showVoiceOptionSelectionDialog("최자두") }

        // 4개 기능 카드 클릭 연결 (2x2 그리드)
        cardLeaderboard.setOnClickListener { showLeaderboardDialog() }
        cardGames.setOnClickListener { showGamesDialog() }
        cardChat.setOnClickListener { showChatDialog() }
        cardSettings.setOnClickListener { showSettingsDialog() }

        // 5. Firebase 실시간 콕찌르기 수신 리스너 등록
        listenForIncomingPokes()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
        } else {
            fetchCurrentLocation()
        }
    }

    private fun fetchCurrentLocation() {
        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) {
                Log.d("Location", "현재 위치 수신 성공: Lat=${loc.latitude}, Lng=${loc.longitude}")
            }
        } catch (e: Exception) {
            Log.e("Location", "위치 정보 수신 실패", e)
        }
    }

    private fun setDialogRatio4to3(dialog: Dialog) {
        dialog.show()
        dialog.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val height = (displayMetrics.heightPixels * 0.85).toInt()
            val width = (height * 4) / 3 // 4 : 3 가로세로 비율
            window.setLayout(width, height)
            window.setGravity(Gravity.CENTER)
            window.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
        }
    }

    // 친구 프로필 및 실시간 플레오스 순정 네비게이션 통합 팝업
    private fun showUnifiedFriendDialog(
        friendName: String,
        handle: String,
        avatarEmoji: String,
        bio: String,
        isDriving: Boolean,
        isOffline: Boolean,
        startLoc: String,
        destLoc: String,
        coords: String,
        speed: String,
        eta: String,
        vehicleInfo: String
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_friend_tabbed, null)

        val tvUnifiedDialogTitle = dialogView.findViewById<TextView>(R.id.tvUnifiedDialogTitle)
        val tvUnifiedStatusBadge = dialogView.findViewById<TextView>(R.id.tvUnifiedStatusBadge)

        val tvProfileAvatar = dialogView.findViewById<TextView>(R.id.tvProfileAvatar)
        val imgProfileAvatar = dialogView.findViewById<ImageView>(R.id.imgProfileAvatar)
        val tvProfileName = dialogView.findViewById<TextView>(R.id.tvProfileName)
        val tvProfileHandle = dialogView.findViewById<TextView>(R.id.tvProfileHandle)
        val tvProfileVehicle = dialogView.findViewById<TextView>(R.id.tvProfileVehicle)
        val tvProfileBio = dialogView.findViewById<TextView>(R.id.tvProfileBio)

        val pleosNavView = dialogView.findViewById<PleosNavSimulationView>(R.id.pleosNavView)

        val btnUnifiedClose = dialogView.findViewById<Button>(R.id.btnUnifiedClose)

        val tvTimelineTitle = dialogView.findViewById<TextView>(R.id.tvTimelineTitle)
        val cardTimelineContainer = dialogView.findViewById<View>(R.id.cardTimelineContainer)
        val timelinePathView = dialogView.findViewById<TimelinePathView>(R.id.timelinePathView)
        timelinePathView?.friendName = friendName

        tvUnifiedDialogTitle.text = "🚗 ${friendName}님의 프로필"
        tvProfileAvatar?.text = avatarEmoji
        imgProfileAvatar?.setImageResource(getAvatarResForFriend(friendName))
        val imgRealNavMap = dialogView.findViewById<ImageView>(R.id.imgRealNavMap)
        val cardNavContainer = dialogView.findViewById<View>(R.id.cardNavContainer)
        imgRealNavMap?.setImageResource(getNavMapResForFriend(friendName))
        tvProfileName.text = friendName
        tvProfileHandle.text = "ID: $handle"
        tvProfileVehicle?.text = "차종: ${getVehicleModelForFriend(friendName)}"
        tvProfileBio.text = "💬 \"$bio\""

        // FusedLocation 엔진 실시간 수신 연동 (지도 화살표 ▲ 및 좌표 텍스트 표출)
        pleosNavView?.let { nav ->
            nav.friendName = friendName
            nav.avatarEmoji = avatarEmoji
            nav.startLocName = startLoc
            nav.destLocName = destLoc
            nav.speedText = speed
            nav.isDriving = isDriving
            nav.updateRealtimeFusedLocation(37.4979, 127.0276, 65f)
        }

        val tvNavSectionTitle = dialogView.findViewById<TextView>(R.id.tvNavSectionTitle)
        val tvNavNoticeText = dialogView.findViewById<TextView>(R.id.tvNavNoticeText)
        val density = resources.displayMetrics.density

        // 타임라인 10명 전원 항상 표출
        tvTimelineTitle?.visibility = View.VISIBLE
        cardTimelineContainer?.visibility = View.VISIBLE

        if (isOffline) {
            tvUnifiedStatusBadge.text = "● 비운행"
            tvUnifiedStatusBadge.setBackgroundColor(0xFFF1F5F9.toInt())
            tvUnifiedStatusBadge.setTextColor(0xFF64748B.toInt())

            tvNavSectionTitle?.visibility = View.VISIBLE
            cardNavContainer?.visibility = View.VISIBLE
            cardNavContainer?.layoutParams?.height = (140 * density).toInt()

            imgRealNavMap?.visibility = View.GONE
            tvNavNoticeText?.visibility = View.VISIBLE
            tvNavNoticeText?.text = "현재 차량 미운행 중입니다."
            tvNavNoticeText?.setTextColor(0xFF64748B.toInt())

        } else if (!isDriving) {
            tvUnifiedStatusBadge.text = "● 정차 중"
            tvUnifiedStatusBadge.setBackgroundColor(0xFFFEF3C7.toInt())
            tvUnifiedStatusBadge.setTextColor(0xFFD97706.toInt())

            tvNavSectionTitle?.visibility = View.VISIBLE
            cardNavContainer?.visibility = View.VISIBLE
            cardNavContainer?.layoutParams?.height = (140 * density).toInt()

            imgRealNavMap?.visibility = View.GONE
            tvNavNoticeText?.visibility = View.VISIBLE
            tvNavNoticeText?.text = "현재 차량 정차 중입니다."
            tvNavNoticeText?.setTextColor(0xFFD97706.toInt())

        } else {
            tvUnifiedStatusBadge.text = "● 주행 중"
            tvUnifiedStatusBadge.setBackgroundColor(0xFFDCFCE7.toInt())
            tvUnifiedStatusBadge.setTextColor(0xFF16A34A.toInt())

            tvNavSectionTitle?.visibility = View.VISIBLE
            cardNavContainer?.visibility = View.VISIBLE
            cardNavContainer?.layoutParams?.height = (480 * density).toInt()

            imgRealNavMap?.visibility = View.VISIBLE
            imgRealNavMap?.setImageResource(getNavMapResForFriend(friendName))
            tvNavNoticeText?.visibility = View.GONE
        }

        // 타임라인 리스트 항목 생성 (10명 전원)
        val layoutTimelineItems = dialogView.findViewById<LinearLayout>(R.id.layoutTimelineItems)
        layoutTimelineItems?.removeAllViews()

        timelinePathView?.visitedNodes?.forEachIndexed { index, node ->
            val itemView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, (12 * density).toInt(), 0, (12 * density).toInt())
            }

            val tvTitle = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = node.title
                setTextColor(0xFF0F172A.toInt())
                textSize = 20f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val tvInfo = TextView(this).apply {
                text = "${node.date} 주행 · ${node.distance.replace(" 주행", "")}"
                setTextColor(0xFF64748B.toInt())
                textSize = 15f
            }

            itemView.addView(tvTitle)
            itemView.addView(tvInfo)
            layoutTimelineItems?.addView(itemView)

            if (index < (timelinePathView?.visitedNodes?.size ?: 0) - 1) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    setBackgroundColor(0xFFF1F5F9.toInt())
                }
                layoutTimelineItems?.addView(divider)
            }
        }

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)

        // 닫기 버튼: Enabled -> Pressed
        btnUnifiedClose.setOnClickListener { dialog.dismiss() }

        setDialogRatio4to3(dialog)
    }

    private fun showEditMyProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_my_profile, null)

        val rgAvatarSelect = dialogView.findViewById<RadioGroup>(R.id.rgAvatarSelect)
        val etMyBioInput = dialogView.findViewById<EditText>(R.id.etMyBioInput)
        val btnCancelEditProfile = dialogView.findViewById<Button>(R.id.btnCancelEditProfile)
        val btnSaveEditProfile = dialogView.findViewById<Button>(R.id.btnSaveEditProfile)

        // 기존 바이오 불러오기
        val currentBioClean = tvMyProfileBio.text.toString()
            .replace("💬 \"", "")
            .replace("\"", "")
        etMyBioInput.setText(currentBioClean)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)

        btnCancelEditProfile.setOnClickListener { dialog.dismiss() }

        btnSaveEditProfile.setOnClickListener {
            val newBio = etMyBioInput.text.toString().trim()
            val chosenAvatar = when (rgAvatarSelect.checkedRadioButtonId) {
                R.id.rbAvatar2 -> "👨‍✈️"
                R.id.rbAvatar3 -> "🏎️"
                R.id.rbAvatar4 -> "👤"
                else -> "🚘"
            }

            tvMyAvatarEmoji.text = chosenAvatar
            tvMyProfileBio.text = "💬 \"$newBio\""

            Toast.makeText(this, "내 SNS 프로필이 성공적으로 수정되었습니다! ✏️", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        setDialogRatio4to3(dialog)
    }

    private fun createCustomTextDialog(title: String, contentText: String): Dialog {
        val dialogView = layoutInflater.inflate(R.layout.dialog_common_text, null)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvDialogContent = dialogView.findViewById<TextView>(R.id.tvDialogContent)
        val btnDialogClose = dialogView.findViewById<Button>(R.id.btnDialogClose)

        tvDialogTitle.text = title
        tvDialogContent.text = contentText

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)

        btnDialogClose.setOnClickListener { dialog.dismiss() }

        return dialog
    }

    // 1번 🏆 이번주 활발이 (주행거리 랭킹 - 세로 2개 파티션: 주간 / 월간)
    private fun showLeaderboardDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_leaderboard, null)
        val btnCloseLeaderboard = dialogView.findViewById<Button>(R.id.btnCloseLeaderboard)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)

        btnCloseLeaderboard.setOnClickListener { dialog.dismiss() }

        setDialogRatio4to3(dialog)
    }

    // 2번 🎮 차량용 미니 게임 선택 메뉴 (오목, 상식퀴즈, 3초 순발력 대결 3개 카드 버튼)
    private fun showGamesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_games_select, null)

        val cardGameOmok = dialogView.findViewById<View>(R.id.cardGameOmok)
        val btnStartOmok = dialogView.findViewById<Button>(R.id.btnStartOmok)

        val cardGameQuiz = dialogView.findViewById<View>(R.id.cardGameQuiz)
        val btnStartQuiz = dialogView.findViewById<Button>(R.id.btnStartQuiz)

        val cardGameWordRelay = dialogView.findViewById<View>(R.id.cardGameWordRelay)
        val btnStartWordRelay = dialogView.findViewById<Button>(R.id.btnStartWordRelay)

        val btnCloseGamesSelect = dialogView.findViewById<Button>(R.id.btnCloseGamesSelect)

        val gamesDialog = Dialog(this)
        gamesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        gamesDialog.setContentView(dialogView)

        // 닫기 버튼: Enabled -> Pressed
        btnCloseGamesSelect.setOnClickListener { gamesDialog.dismiss() }

        // [1번 오목 대결 - 게임 버튼 누르면 대결할 친구 선택 창 오픈] (Enabled -> Pressed)
        val openOmokInvite = View.OnClickListener {
            gamesDialog.dismiss()
            showFriendInviteDialog("⚪⚫ 1:1 오목 대결", "OMOK")
        }
        cardGameOmok.setOnClickListener(openOmokInvite)
        btnStartOmok.setOnClickListener(openOmokInvite)

        // [2번 상식 퀴즈 - 게임 버튼 누르면 대결할 친구 선택 창 오픈] (Enabled -> Pressed)
        val openQuizInvite = View.OnClickListener {
            gamesDialog.dismiss()
            showFriendInviteDialog("🧠 상식 퀴즈", "QUIZ")
        }
        cardGameQuiz.setOnClickListener(openQuizInvite)
        btnStartQuiz.setOnClickListener(openQuizInvite)

        // [3번 3초 순발력 대결 - 게임 버튼 누르면 대결할 친구 선택 창 오픈] (Enabled -> Pressed)
        val openWordRelayInvite = View.OnClickListener {
            gamesDialog.dismiss()
            showFriendInviteDialog("🎯 3초 순발력 대결", "SPEED")
        }
        cardGameWordRelay.setOnClickListener(openWordRelayInvite)
        btnStartWordRelay.setOnClickListener(openWordRelayInvite)

        setDialogRatio4to3(gamesDialog)
    }

    // 대결할 친구 선택 (대결 신청) 팝업 (신청 버튼 클릭 시: Enabled -> Pressed -> Loading 상태 변화!)
    private fun showFriendInviteDialog(gameTitle: String, gameType: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_game_friend_invite, null)

        val btnBackFromInvite = dialogView.findViewById<Button>(R.id.btnBackFromInvite)
        val tvInviteTitle = dialogView.findViewById<TextView>(R.id.tvInviteTitle)
        val btnCloseInvite = dialogView.findViewById<Button>(R.id.btnCloseInvite)

        val btnInviteFriend1 = dialogView.findViewById<Button>(R.id.btnInviteFriend1)
        val btnInviteFriend2 = dialogView.findViewById<Button>(R.id.btnInviteFriend2)
        val btnInviteFriend3 = dialogView.findViewById<Button>(R.id.btnInviteFriend3)
        val btnInviteFriend5 = dialogView.findViewById<Button>(R.id.btnInviteFriend5)
        val btnInviteFriend6 = dialogView.findViewById<Button>(R.id.btnInviteFriend6)
        val btnInviteFriend7 = dialogView.findViewById<Button>(R.id.btnInviteFriend7)
        val btnInviteFriend9 = dialogView.findViewById<Button>(R.id.btnInviteFriend9)
        val btnInviteFriend10 = dialogView.findViewById<Button>(R.id.btnInviteFriend10)

        tvInviteTitle.text = "⚔️ $gameTitle - 친구 선택"

        val inviteDialog = Dialog(this)
        inviteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        inviteDialog.setContentView(dialogView)

        btnBackFromInvite.setOnClickListener {
            inviteDialog.dismiss()
            showGamesDialog()
        }

        btnCloseInvite.setOnClickListener {
            inviteDialog.dismiss()
        }

        // [⚔️ 신청] 버튼 상태 변화: Enabled -> Pressed -> Loading (전송 중...) -> 연결 완료!
        val launchTargetGameWithLoading = { targetFriend: String, clickedBtn: Button? ->
            clickedBtn?.let { btn ->
                btn.text = "⌛ 신청 중..."
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFCBD5E1.toInt())
                btn.setTextColor(0xFF1D1E21.toInt())
                btn.isEnabled = false
            }

            Handler(Looper.getMainLooper()).postDelayed({
                inviteDialog.dismiss()

                // 1. 대결 신청 3초 로딩 팝업창 오픈
                val loadingView = layoutInflater.inflate(R.layout.dialog_game_connecting, null)
                val tvConnectingMessage = loadingView.findViewById<TextView>(R.id.tvConnectingMessage)
                tvConnectingMessage.text = "'${targetFriend}'님에게 게임을 신청하는 중..."

                val loadingDialog = Dialog(this)
                loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                loadingDialog.setContentView(loadingView)
                loadingDialog.setCancelable(false)
                setDialogRatio4to3(loadingDialog)

                // 2. 3초 후 상대방 수락 토스트 및 게임 화면 전환
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingDialog.dismiss()
                    Toast.makeText(this, "🎉 ${targetFriend}님이 대결 신청을 수락했습니다!", Toast.LENGTH_SHORT).show()

                    when (gameType) {
                        "OMOK" -> showOmokGameDialog(targetFriend)
                        "QUIZ" -> showQuizGameDialog(targetFriend)
                        else -> showWordRelayGameDialog()
                    }
                }, 3000)
            }, 600)
        }

        btnInviteFriend1?.setOnClickListener { launchTargetGameWithLoading("김플레오", btnInviteFriend1) }
        btnInviteFriend3?.setOnClickListener { launchTargetGameWithLoading("이현대", btnInviteFriend3) }
        btnInviteFriend5?.setOnClickListener { launchTargetGameWithLoading("성주은", btnInviteFriend5) }
        btnInviteFriend6?.setOnClickListener { launchTargetGameWithLoading("이예온", btnInviteFriend6) }
        btnInviteFriend9?.setOnClickListener { launchTargetGameWithLoading("김국민", btnInviteFriend9) }
        btnInviteFriend2?.setOnClickListener { launchTargetGameWithLoading("김플레오맘", btnInviteFriend2) }
        btnInviteFriend7?.setOnClickListener { launchTargetGameWithLoading("엄성현", btnInviteFriend7) }
        btnInviteFriend10?.setOnClickListener { launchTargetGameWithLoading("최자두", btnInviteFriend10) }

        setDialogRatio4to3(inviteDialog)
    }

    // 2-1. ⚪⚫ 1:1 두뇌 오목 대결 팝업 (18x18 정밀 격자 바둑판 & 🏳️ 기권하기 버튼)
    private fun showOmokGameDialog(friendName: String = "상대방") {
        val dialogView = layoutInflater.inflate(R.layout.dialog_game_omok, null)

        val btnBackFromOmok = dialogView.findViewById<Button>(R.id.btnBackFromOmok)
        val btnResetOmok = dialogView.findViewById<Button>(R.id.btnResetOmok)
        val tvOmokStatusText = dialogView.findViewById<TextView>(R.id.tvOmokStatusText)
        val omokBoardView = dialogView.findViewById<OmokBoardView>(R.id.omokBoardView)

        val omokDialog = Dialog(this)
        omokDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        omokDialog.setContentView(dialogView)

        btnBackFromOmok.setOnClickListener {
            omokDialog.dismiss()
            showGamesDialog()
        }

        // [🏳️ 기권하기] 버튼 클릭 시 -> 상대방 승리 팝업창 오픈!
        btnResetOmok.setOnClickListener {
            showOmokResultDialog(friendName, friendName, omokDialog, omokBoardView)
        }

        omokBoardView.onTurnChangeListener = { isUserTurn, statusMsg ->
            tvOmokStatusText.text = statusMsg
            if (omokBoardView.isGameOver) {
                val winner = if (isUserTurn) friendName else "내 차(User)"
                showOmokResultDialog(winner, friendName, omokDialog, omokBoardView)
            }
        }

        setDialogRatio4to3(omokDialog)
    }

    // 오목 승리 결과 팝업창 (중앙 컴팩트 팝업, 60% 높이 차지하는 대형 승리자 텍스트, [게임 재신청] & [닫기] 버튼)
    private fun showOmokResultDialog(winnerName: String, friendName: String, omokDialog: Dialog, omokBoardView: OmokBoardView) {
        val resultView = layoutInflater.inflate(R.layout.dialog_omok_result, null)

        val tvOmokWinnerTitle = resultView.findViewById<TextView>(R.id.tvOmokWinnerTitle)
        val btnReplayOmok = resultView.findViewById<Button>(R.id.btnReplayOmok)
        val btnCloseOmokResult = resultView.findViewById<Button>(R.id.btnCloseOmokResult)

        tvOmokWinnerTitle.text = "${winnerName} 승!"

        val resultDialog = Dialog(this)
        resultDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        resultDialog.setContentView(resultView)

        // 컴팩트 팝업 크기 세팅
        resultDialog.show()
        resultDialog.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val height = (displayMetrics.heightPixels * 0.48).toInt()
            val width = (height * 4) / 3
            window.setLayout(width, height)
            window.setGravity(Gravity.CENTER)
            window.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
        }

        // [🔄 게임 재신청] 버튼
        btnReplayOmok.setOnClickListener {
            resultDialog.dismiss()
            omokBoardView.resetBoard()
            Toast.makeText(this, "🎮 ${friendName}님과 새 게임을 시작합니다!", Toast.LENGTH_SHORT).show()
        }

        // [✖️ 닫기] 버튼
        btnCloseOmokResult.setOnClickListener {
            resultDialog.dismiss()
            omokDialog.dismiss()
            showGamesDialog()
        }
    }

    // 2-2. 🧠 상식 퀴즈 팝업 (버튼 반투명 초록/빨강 색상 변경 & 중앙 팝업 연동)
    private fun showQuizGameDialog(friendName: String = "상대방") {
        val dialogView = layoutInflater.inflate(R.layout.dialog_game_quiz, null)

        val btnBackFromQuiz = dialogView.findViewById<Button>(R.id.btnBackFromQuiz)
        val btnCloseQuiz = dialogView.findViewById<Button>(R.id.btnCloseQuiz)
        val tvQuizScoreBadge = dialogView.findViewById<TextView>(R.id.tvQuizScoreBadge)

        val tvQuizQuestionNo = dialogView.findViewById<TextView>(R.id.tvQuizQuestionNo)
        val tvQuizQuestionText = dialogView.findViewById<TextView>(R.id.tvQuizQuestionText)

        val btnChoiceA = dialogView.findViewById<Button>(R.id.btnChoiceA)
        val btnChoiceB = dialogView.findViewById<Button>(R.id.btnChoiceB)
        val btnChoiceC = dialogView.findViewById<Button>(R.id.btnChoiceC)
        val btnChoiceD = dialogView.findViewById<Button>(R.id.btnChoiceD)

        val choiceButtons = listOf(btnChoiceA, btnChoiceB, btnChoiceC, btnChoiceD)

        val quizDialog = Dialog(this)
        quizDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        quizDialog.setContentView(dialogView)

        btnBackFromQuiz.setOnClickListener {
            quizDialog.dismiss()
            showGamesDialog()
        }

        btnCloseQuiz.setOnClickListener {
            quizDialog.dismiss()
        }

        var currentRound = 1
        var userScore = 20

        fun resetChoiceButtonColors() {
            for (btn in choiceButtons) {
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFFFFFF.toInt())
                btn.setTextColor(0xFF1D1E21.toInt())
                btn.isEnabled = true
            }
        }

        fun loadQuestionRound(round: Int) {
            currentRound = round
            resetChoiceButtonColors()

            if (round == 1) {
                tvQuizQuestionNo.text = "Q1. 한국 역사 상식 퀴즈"
                tvQuizQuestionText.text = "대한민국 임시정부 초대 대통령이자 대한민국 제1·2·3대 대통령은 누구일까요?"
                btnChoiceA.text = "A. 이승만"
                btnChoiceB.text = "B. 김구"
                btnChoiceC.text = "C. 안창호"
                btnChoiceD.text = "D. 윤보선"
            } else {
                tvQuizQuestionNo.text = "Q2. 세계 지리 상식 퀴즈"
                tvQuizQuestionText.text = "프랑스의 수도이자 에펠탑과 루브르 박물관이 위치한 도시는 어디일까요?"
                btnChoiceA.text = "A. 런던"
                btnChoiceB.text = "B. 파리"
                btnChoiceC.text = "C. 베를린"
                btnChoiceD.text = "D. 로마"
            }
        }

        loadQuestionRound(1)

        val onChoiceSelected = View.OnClickListener { clickedView ->
            // 버튼 클릭 비활성화
            for (btn in choiceButtons) btn.isEnabled = false

            val userChoiceBtn = clickedView as Button
            val realAnswerIndex = if (currentRound == 1) 0 else 1 // Q1 -> A(0), Q2 -> B(1)
            val realAnswerBtn = choiceButtons[realAnswerIndex]

            val isUserCorrect = (userChoiceBtn == realAnswerBtn)

            // 1. 색상 하이라이트 적용 (반투명 초록: 0x8010B981, 반투명 빨강: 0x80EF4444)
            if (isUserCorrect) {
                userChoiceBtn.backgroundTintList = android.content.res.ColorStateList.valueOf(0x8010B981.toInt()) // 반투명 초록
            } else {
                userChoiceBtn.backgroundTintList = android.content.res.ColorStateList.valueOf(0x80EF4444.toInt()) // 반투명 빨강
                realAnswerBtn.backgroundTintList = android.content.res.ColorStateList.valueOf(0x8010B981.toInt()) // 정답 반투명 초록
            }

            // 2. 1초 후 친구 답변 대기 및 결과 처리
            Handler(Looper.getMainLooper()).postDelayed({
                val isFriendCorrect = if (currentRound == 1) isUserCorrect else false

                if (isUserCorrect && isFriendCorrect) {
                    // 케이스 A: 둘 다 맞춤 ➔ 중앙 [다음 문제로 ➡️] 팝업
                    val navView = layoutInflater.inflate(R.layout.dialog_quiz_nav, null)
                    val tvQuizNavMessage = navView.findViewById<TextView>(R.id.tvQuizNavMessage)
                    val btnQuizNavAction = navView.findViewById<Button>(R.id.btnQuizNavAction)

                    tvQuizNavMessage.text = "🎉 둘 다 정답입니다!\n다음 문제로 이동하세요."
                    btnQuizNavAction.text = "다음 문제로 ➡️"

                    val navDialog = Dialog(this)
                    navDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    navDialog.setContentView(navView)

                    navDialog.show()
                    navDialog.window?.let { window ->
                        val displayMetrics = resources.displayMetrics
                        val height = (displayMetrics.heightPixels * 0.38).toInt()
                        val width = (height * 4) / 3
                        window.setLayout(width, height)
                        window.setGravity(Gravity.CENTER)
                        window.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
                    }

                    btnQuizNavAction.setOnClickListener {
                        navDialog.dismiss()
                        userScore += 20
                        tvQuizScoreBadge.text = "⭐ 점수: ${userScore}점"
                        loadQuestionRound(2)
                    }
                } else {
                    // 케이스 B: 틀린 사람이 있음 ➔ 중앙 [결과 확인하기 🏆] 팝업
                    val winnerName = when {
                        isUserCorrect && !isFriendCorrect -> "내 차(User)"
                        !isUserCorrect && isFriendCorrect -> friendName
                        else -> friendName
                    }

                    val navView = layoutInflater.inflate(R.layout.dialog_quiz_nav, null)
                    val tvQuizNavMessage = navView.findViewById<TextView>(R.id.tvQuizNavMessage)
                    val btnQuizNavAction = navView.findViewById<Button>(R.id.btnQuizNavAction)

                    tvQuizNavMessage.text = "🏁 게임이 종료됐습니다!\n결과를 확인하세요."
                    btnQuizNavAction.text = "결과 확인하기 🏆"

                    val navDialog = Dialog(this)
                    navDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    navDialog.setContentView(navView)

                    navDialog.show()
                    navDialog.window?.let { window ->
                        val displayMetrics = resources.displayMetrics
                        val height = (displayMetrics.heightPixels * 0.38).toInt()
                        val width = (height * 4) / 3
                        window.setLayout(width, height)
                        window.setGravity(Gravity.CENTER)
                        window.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
                    }

                    btnQuizNavAction.setOnClickListener {
                        navDialog.dismiss()
                        showQuizWinnerDialog(winnerName, friendName, quizDialog)
                    }
                }
            }, 1000)
        }

        btnChoiceA.setOnClickListener(onChoiceSelected)
        btnChoiceB.setOnClickListener(onChoiceSelected)
        btnChoiceC.setOnClickListener(onChoiceSelected)
        btnChoiceD.setOnClickListener(onChoiceSelected)

        setDialogRatio4to3(quizDialog)
    }

    // 상식 퀴즈 최종 승리자 팝업창 ('정답 혼자 맞힌 사람' 승 60% 높이 텍스트, [게임 재신청] & [닫기] 버튼)
    private fun showQuizWinnerDialog(winnerName: String, friendName: String, quizDialog: Dialog) {
        val winnerView = layoutInflater.inflate(R.layout.dialog_quiz_winner, null)

        val tvQuizWinnerTitle = winnerView.findViewById<TextView>(R.id.tvQuizWinnerTitle)
        val btnReplayQuiz = winnerView.findViewById<Button>(R.id.btnReplayQuiz)
        val btnCloseQuizWinner = winnerView.findViewById<Button>(R.id.btnCloseQuizWinner)

        tvQuizWinnerTitle.text = "${winnerName} 승!"

        val winnerDialog = Dialog(this)
        winnerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        winnerDialog.setContentView(winnerView)

        winnerDialog.show()
        winnerDialog.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val height = (displayMetrics.heightPixels * 0.48).toInt()
            val width = (height * 4) / 3
            window.setLayout(width, height)
            window.setGravity(Gravity.CENTER)
            window.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
        }

        btnReplayQuiz.setOnClickListener {
            winnerDialog.dismiss()
            Toast.makeText(this, "🎮 ${friendName}님과 새 상식 퀴즈를 시작합니다!", Toast.LENGTH_SHORT).show()
        }

        btnCloseQuizWinner.setOnClickListener {
            winnerDialog.dismiss()
            quizDialog.dismiss()
            showGamesDialog()
        }
    }

    // 2-3. 🎯 3초 순발력 대결 팝업
    private fun showWordRelayGameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_game_word_relay, null)

        val btnBackFromWordRelay = dialogView.findViewById<Button>(R.id.btnBackFromWordRelay)
        val btnCloseWordRelay = dialogView.findViewById<Button>(R.id.btnCloseWordRelay)
        val tvWordScoreBadge = dialogView.findViewById<TextView>(R.id.tvWordScoreBadge)
        val tvWordLog = dialogView.findViewById<TextView>(R.id.tvWordLog)

        val etWordInput = dialogView.findViewById<EditText>(R.id.etWordInput)
        val btnSubmitWord = dialogView.findViewById<Button>(R.id.btnSubmitWord)

        val wordDialog = Dialog(this)
        wordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        wordDialog.setContentView(dialogView)

        btnBackFromWordRelay.setOnClickListener {
            wordDialog.dismiss()
            showGamesDialog()
        }

        btnCloseWordRelay.setOnClickListener {
            wordDialog.dismiss()
        }

        var successCount = 3

        btnSubmitWord.setOnClickListener {
            val inputStr = etWordInput.text.toString().trim()
            if (inputStr.isNotEmpty()) {
                successCount++
                tvWordScoreBadge.text = "⭐ 성공: ${successCount}회"
                val newLog = tvWordLog.text.toString() + "\n• 내 차(User): $inputStr (순발력 터치 성공! +10점)"
                tvWordLog.text = newLog
                etWordInput.setText("")
                Toast.makeText(this, "🎉 순발력 성공! '$inputStr' 달성!", Toast.LENGTH_SHORT).show()
            }
        }

        setDialogRatio4to3(wordDialog)
    }

    // 3번 💬 실시간 채팅 목록 (8명 각각 누르면 목록 팝업을 닫고 1:1 대화방 오픈!)
    private fun showChatDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_chat_view, null)

        val btnCloseChat = dialogView.findViewById<Button>(R.id.btnCloseChat)

        val cardChatItem1 = dialogView.findViewById<View>(R.id.cardChatItem1)
        val cardChatItem2 = dialogView.findViewById<View>(R.id.cardChatItem2)
        val cardChatItem3 = dialogView.findViewById<View>(R.id.cardChatItem3)
        val cardChatItem4 = dialogView.findViewById<View>(R.id.cardChatItem4)
        val cardChatItem5 = dialogView.findViewById<View>(R.id.cardChatItem5)
        val cardChatItem6 = dialogView.findViewById<View>(R.id.cardChatItem6)
        val cardChatItem7 = dialogView.findViewById<View>(R.id.cardChatItem7)
        val cardChatItem8 = dialogView.findViewById<View>(R.id.cardChatItem8)

        val listDialog = Dialog(this)
        listDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        listDialog.setContentView(dialogView)

        btnCloseChat.setOnClickListener { listDialog.dismiss() }

        // 성주은, 김플레오, 이현대: 상대방 메시지가 마지막 (isMyMessage = false)
        // 이예온, 김국민, 김플레오맘, 엄성현, 최자두: 내가 보낸 메시지가 마지막 (isMyMessage = true)
        cardChatItem1?.setOnClickListener {
            listDialog.dismiss()
            showDirectChatRoomDialog("성주은", "⚡", "● 주행 중 · AVANTE N", "도착하면 카페로 와", isMyMessage = false)
        }
        cardChatItem2?.setOnClickListener {
            listDialog.dismiss()
            showDirectChatRoomDialog("김플레오", "👨‍✈️", "● 주행 중 · IONIQ 6", "너 어디임??", isMyMessage = false)
        }
        cardChatItem3?.setOnClickListener {
            listDialog.dismiss()
            showDirectChatRoomDialog("이현대", "🏎️", "● 주행 중 · EV6", "이현대님이 회원님을 콕 찔렀습니다!", isMyMessage = false)
        }
        cardChatItem4?.setOnClickListener {
            listDialog.dismiss()
            showDirectChatRoomDialog("이예온", "🚘", "● 주행 중 · SANTA FE", "예온아 너 가평이야?", isMyMessage = true)
        }
        cardChatItem5?.setOnClickListener {
            listDialog.dismiss()
            showDirectChatRoomDialog("김국민", "🌄", "● 주행 중 · CASPER", "그럼 3시에 국민대에서 보자!!", isMyMessage = true)
        }
        cardChatItem6?.setOnClickListener {
            listDialog.dismiss()
            showDirectChatRoomDialog("김플레오맘", "👤", "● 정차 중 🅿️ · PALISADE", "네 오늘 플레오 저랑 만나기로 했어요", isMyMessage = true)
        }
        cardChatItem7?.setOnClickListener {
            listDialog.dismiss()
            showDirectChatRoomDialog("엄성현", "👔", "● 정차 중 🅿️ · TUCSON", "오늘도 좋은 하루 보내~", isMyMessage = true)
        }
        cardChatItem8?.setOnClickListener {
            listDialog.dismiss()
            showDirectChatRoomDialog("최자두", "🐤", "● 정차 중 🅿️ · RAY EV", "이제 진짜 가을이지...", isMyMessage = true)
        }

        setDialogRatio4to3(listDialog)
    }

    // 1:1 모던 화이트/블루 실시간 대화방 팝업 (isMyMessage 옵션에 따라 좌측 상대방 말풍선 또는 우측 내 말풍선 추가)
    private fun showDirectChatRoomDialog(
        friendName: String,
        avatarEmoji: String,
        statusInfo: String,
        initialMessage: String,
        isMyMessage: Boolean = false
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_direct_chat_room, null)

        val btnBackToChatList = dialogView.findViewById<Button>(R.id.btnBackToChatList)
        val tvRoomAvatar = dialogView.findViewById<TextView>(R.id.tvRoomAvatar)
        val tvRoomFriendName = dialogView.findViewById<TextView>(R.id.tvRoomFriendName)
        val tvRoomFriendStatus = dialogView.findViewById<TextView>(R.id.tvRoomFriendStatus)
        val btnCloseRoom = dialogView.findViewById<Button>(R.id.btnCloseRoom)

        val scrollChatRoom = dialogView.findViewById<ScrollView>(R.id.scrollChatRoom)
        val layoutRoomMessagesContainer = dialogView.findViewById<LinearLayout>(R.id.layoutRoomMessagesContainer)

        val etRoomInput = dialogView.findViewById<EditText>(R.id.etRoomInput)
        val btnRoomSend = dialogView.findViewById<Button>(R.id.btnRoomSend)

        tvRoomAvatar?.text = avatarEmoji
        val imgRoomAvatar = dialogView.findViewById<ImageView>(R.id.imgRoomAvatar)
        imgRoomAvatar?.setImageResource(getAvatarResForFriend(friendName))
        tvRoomFriendName.text = friendName
        tvRoomFriendStatus.text = statusInfo

        // 동적으로 초기 메시지 말풍선 추가
        if (isMyMessage) {
            val myBubbleView = layoutInflater.inflate(R.layout.item_my_chat_bubble, null)
            val tvMyMsg = myBubbleView.findViewById<TextView>(R.id.tvMyMsgText)
            val tvMyTime = myBubbleView.findViewById<TextView>(R.id.tvMyMsgTime)

            tvMyMsg.text = initialMessage
            tvMyTime.text = "오전 10:15"

            layoutRoomMessagesContainer.addView(myBubbleView)
        } else {
            val otherBubbleView = layoutInflater.inflate(R.layout.item_other_chat_bubble, null)
            val tvBubbleAvatar = otherBubbleView.findViewById<TextView>(R.id.tvBubbleAvatar)
            val imgBubbleAvatar = otherBubbleView.findViewById<ImageView>(R.id.imgBubbleAvatar)
            val tvBubbleSender = otherBubbleView.findViewById<TextView>(R.id.tvBubbleSender)
            val tvBubbleText = otherBubbleView.findViewById<TextView>(R.id.tvBubbleText)

            tvBubbleAvatar?.text = avatarEmoji
            imgBubbleAvatar?.setImageResource(getAvatarResForFriend(friendName))
            tvBubbleSender.text = friendName
            tvBubbleText.text = initialMessage

            layoutRoomMessagesContainer.addView(otherBubbleView)
        }

        val roomDialog = Dialog(this)
        roomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        roomDialog.setContentView(dialogView)

        // 키보드가 올라올 때 대화창이 키보드 바로 위까지 길게 확장되고 background는 깔끔하게 처리
        roomDialog.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val fullHeight = (displayMetrics.heightPixels * 0.85).toInt()
            val compactHeight = (displayMetrics.heightPixels * 0.46).toInt()
            val width = (fullHeight * 4) / 3

            window.setLayout(width, fullHeight)
            window.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            window.setBackgroundDrawableResource(R.drawable.bg_dialog_white)

            etRoomInput.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    window.setLayout(width, compactHeight)
                    window.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                    scrollChatRoom.postDelayed({
                        scrollChatRoom.fullScroll(ScrollView.FOCUS_DOWN)
                    }, 150)
                } else {
                    window.setLayout(width, fullHeight)
                    window.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                }
            }
        }

        // ◀️ 목록 버튼 누르면 1:1 채팅방 닫고 채팅 목록 다시 열기
        btnBackToChatList.setOnClickListener {
            roomDialog.dismiss()
            showChatDialog()
        }

        btnCloseRoom.setOnClickListener {
            roomDialog.dismiss()
        }

        // 💬 [전송] 버튼 누르면 1:1 대화방에 우측 블루 말풍선 실시간 추가!
        btnRoomSend.setOnClickListener {
            val userText = etRoomInput.text.toString().trim()
            if (userText.isNotEmpty()) {
                val currentTime = SimpleDateFormat("a hh:mm", Locale.KOREA).format(Date())

                // 우측 배치 내 말풍선 레이아웃 추가
                val myBubbleView = layoutInflater.inflate(R.layout.item_my_chat_bubble, null)
                val tvMyMsg = myBubbleView.findViewById<TextView>(R.id.tvMyMsgText)
                val tvMyTime = myBubbleView.findViewById<TextView>(R.id.tvMyMsgTime)

                tvMyMsg.text = userText
                tvMyTime.text = currentTime

                layoutRoomMessagesContainer.addView(myBubbleView)
                etRoomInput.setText("")

                // 자동 스크롤 하단 이동
                scrollChatRoom.post {
                    scrollChatRoom.fullScroll(ScrollView.FOCUS_DOWN)
                }

                Toast.makeText(this, "${friendName}님에게 메시지가 전송되었습니다! 💬", Toast.LENGTH_SHORT).show()
            }
        }

        roomDialog.show()
    }

    // 6번 ⚙️ 설정 (톱니바퀴)
    private fun showSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)

        val switchDarkMode = dialogView.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchDarkMode)
        val switchSoundEffects = dialogView.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchSoundEffects)
        val switchGpsSync = dialogView.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchGpsSync)
        val btnCloseSettings = dialogView.findViewById<Button>(R.id.btnCloseSettings)

        // 다크 모드 / 라이트 모드 전환 이벤트
        switchDarkMode?.setOnCheckedChangeListener { _, isChecked ->
            val modeName = if (isChecked) "다크 모드" else "라이트 모드"
            Toast.makeText(this, "화면 테마가 $modeName 로 전환되었습니다.", Toast.LENGTH_SHORT).show()

            val rootLayout = findViewById<View>(android.R.id.content)
            if (isChecked) {
                rootLayout?.setBackgroundColor(0xFF0F172A.toInt()) // Dark Slate
            } else {
                rootLayout?.setBackgroundColor(0xFFF8FAFC.toInt()) // Light Slate
            }
        }

        switchSoundEffects?.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "[ON]" else "[OFF]"
            Toast.makeText(this, "효과음 및 음향이 $status 설정되었습니다.", Toast.LENGTH_SHORT).show()
        }

        switchGpsSync?.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "[ON]" else "[OFF]"
            Toast.makeText(this, "GPS 자동 동기화가 $status 설정되었습니다.", Toast.LENGTH_SHORT).show()
        }

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)

        btnCloseSettings.setOnClickListener { dialog.dismiss() }

        setDialogRatio4to3(dialog)
    }

    private fun fetchVehicleDoorStatus() {
        tvDoorStatus.text = "운전석 도어: 조회 중..."
        val door = vehicle.getDoor()
        door.getCurrentDoorPosition(
            area = DoorArea(DoorValue.ROW_1_LEFT),
            onComplete = { pos: Int? ->
                currentDoorPositionVal = pos
                val doorText = if (pos == 0) "닫힘 (0)" else "열림 ($pos)"
                tvDoorStatus.text = "운전석 도어: $doorText"
                Log.d("VehicleSDK", "현재 도어 위치: $pos")
            },
            onFailed = { e: Exception ->
                tvDoorStatus.text = "운전석 도어: 조회 실패"
                Log.e("VehicleSDK", "도어 위치 조회 실패", e)
            }
        )
    }

    private fun showVoiceOptionSelectionDialog(friendName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_voice_option_select, null)

        val tvVoiceTitle = dialogView.findViewById<TextView>(R.id.tvVoiceTitle)
        val cardMicSpeak = dialogView.findViewById<View>(R.id.cardMicSpeak)
        val cardPresetList = dialogView.findViewById<View>(R.id.cardPresetList)
        val btnCloseVoiceOption = dialogView.findViewById<Button>(R.id.btnCloseVoiceOption)

        tvVoiceTitle.text = "🎙️ ${friendName}님에게 음성 전송"

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)

        cardMicSpeak.setOnClickListener {
            dialog.dismiss()
            showMicDirectSpeakDialog(friendName)
        }

        cardPresetList.setOnClickListener {
            dialog.dismiss()
            showVoiceQuickListDialog(friendName)
        }

        btnCloseVoiceOption.setOnClickListener {
            dialog.dismiss()
        }

        setDialogRatio4to3(dialog)
    }

    private fun showMicDirectSpeakDialog(friendName: String) {
        // RECORD_AUDIO 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                2001
            )
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_mic_recording, null)
        val tvFriendTarget = dialogView.findViewById<TextView>(R.id.tvFriendTarget)
        val etSpeechResult = dialogView.findViewById<TextView>(R.id.etSpeechResult)
        val audioWaveformView = dialogView.findViewById<AudioWaveformView>(R.id.audioWaveformView)
        val tvVoiceTimer = dialogView.findViewById<TextView>(R.id.tvVoiceTimer)
        val btnSendVoice = dialogView.findViewById<Button>(R.id.btnSendVoice)
        val btnCloseVoice = dialogView.findViewById<Button>(R.id.btnCloseVoice)

        tvFriendTarget.text = "🎯 받는 친구: $friendName"
        etSpeechResult.text = "🎤 마이크에 대고 말씀하세요..."

        // 파형 애니메이션 시작
        audioWaveformView?.startAnimation()

        // 타이머 애니메이션 (00:00.00 카운트업)
        var elapsedMs = 0L
        val handler = Handler(Looper.getMainLooper())
        val timerRunnable = object : Runnable {
            override fun run() {
                elapsedMs += 70L
                val secs = elapsedMs / 1000L
                val millis = (elapsedMs % 1000L) / 10L
                val timerStr = String.format(Locale.getDefault(), "%02d:%02d.%02d", secs / 60, secs % 60, millis)
                tvVoiceTimer?.text = timerStr
                handler.postDelayed(this, 70L)
            }
        }
        handler.post(timerRunnable)

        // Pleos SpeechToText SDK ResultListener 연결
        val sttResultListener = object : ai.pleos.playground.stt.listener.ResultListener {
            override fun onReady() {
                Log.d("STT", "SpeechToText 마이크 수신 준비 완료")
            }

            override fun onStartedRecognition() {
                Log.d("STT", "SpeechToText 마이크 수신 시작")
            }

            override fun onUpdated(stt: String, completed: Boolean) {
                runOnUiThread {
                    if (stt.isNotEmpty()) {
                        etSpeechResult.text = stt
                    }
                }
            }

            override fun onUpdatedRms(rms: Double) {}

            override fun onUpdatedEpdData(on: Long, off: Long) {}

            override fun onError(errorType: ai.pleos.playground.stt.constant.ErrorType) {
                Log.e("STT", "SpeechToText 수신 오류: $errorType")
            }

            override fun onEndedRecognition() {
                Log.d("STT", "SpeechToText 마이크 수신 종료")
            }
        }

        try {
            speechToText.addListener(sttResultListener)
        } catch (e: Exception) {
            Log.e("STT", "addListener error: ${e.message}")
        }

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)

        dialog.setOnDismissListener {
            audioWaveformView?.stopAnimation()
            handler.removeCallbacks(timerRunnable)
            try {
                speechToText.completeAudioSend()
                speechToText.removeListener(sttResultListener)
            } catch (ignored: Exception) {}
        }

        btnSendVoice?.setOnClickListener {
            val recognizedText = etSpeechResult.text.toString().ifEmpty { "안전 운전하세요!" }
            Toast.makeText(
                this,
                "🎙️ ${friendName}님에게 음성 메시지(\"$recognizedText\")를 성공적으로 전송했습니다!",
                Toast.LENGTH_LONG
            ).show()
            dialog.dismiss()
        }

        btnCloseVoice?.setOnClickListener {
            dialog.dismiss()
        }

        setDialogRatio4to3(dialog)
    }

    private fun showVoiceQuickListDialog(friendName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_voice_quick_list, null)

        val tvQuickListTitle = dialogView.findViewById<TextView>(R.id.tvQuickListTitle)
        val rgQuickMessages = dialogView.findViewById<RadioGroup>(R.id.rgQuickMessages)
        val btnBackToOption = dialogView.findViewById<Button>(R.id.btnBackToOption)
        val btnSendVoiceQuick = dialogView.findViewById<Button>(R.id.btnSendVoiceQuick)

        tvQuickListTitle.text = "${friendName}님에게 전송할 음성 선택"

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)

        btnBackToOption.setOnClickListener {
            dialog.dismiss()
            showVoiceOptionSelectionDialog(friendName)
        }

        btnSendVoiceQuick.setOnClickListener {
            val checkedId = rgQuickMessages.checkedRadioButtonId
            val selectedRb = dialogView.findViewById<RadioButton>(checkedId)
            val chosenMessage = selectedRb?.text?.toString() ?: "안전 운전하세요~!"

            dialog.dismiss()
            sendVoiceMessageToFriend(friendName, chosenMessage)
        }

        setDialogRatio4to3(dialog)
    }

    private fun sendVoiceMessageToFriend(friendName: String, messageText: String) {
        val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val doorInfo = if (currentDoorPositionVal == 0) "문 닫힘" else "문 열림"

        val pokeData = hashMapOf(
            "sender" to "내 차(User)",
            "receiver" to friendName,
            "type" to "VOICE",
            "message" to "🎙️ \"$messageText\"",
            "vehicleStatus" to doorInfo,
            "timestamp" to System.currentTimeMillis(),
            "timeString" to timeString
        )

        db.collection("pokes")
            .add(pokeData)
            .addOnSuccessListener {
                Toast.makeText(this, "${friendName}님에게 음성 메시지를 전송했습니다! 🎙️", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "전송 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("CarSNS", "Firebase send error", e)
            }
    }

    // 콕찌르기 상태 변화 (Enabled -> Pressed -> Loading -> Disabled)
    private fun sendPokeToFriend(friendName: String, btnPoke: Button? = null) {
        btnPoke?.let { btn ->
            btn.text = "⌛ 콕찌르기..."
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFCBD5E1.toInt())
            btn.setTextColor(0xFF1D1E21.toInt())
            btn.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({
                btn.text = "✅ 찌르기 완료"
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFF1F5F9.toInt())
                btn.setTextColor(0xFF94A3B8.toInt())
            }, 1500)
        }

        val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val doorInfo = if (currentDoorPositionVal == 0) "문 닫힘" else "문 열림"

        val pokeData = hashMapOf(
            "sender" to "내 차(User)",
            "receiver" to friendName,
            "type" to "POKE",
            "message" to "👉 ${friendName}님을 콕 찔렀습니다!",
            "vehicleStatus" to doorInfo,
            "timestamp" to System.currentTimeMillis(),
            "timeString" to timeString
        )

        db.collection("pokes")
            .add(pokeData)
            .addOnSuccessListener {
                Toast.makeText(this, "${friendName}님을 콕 찔렀습니다! 👉", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "콕찌르기 전송 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("CarSNS", "Firebase send error", e)
            }
    }

    private fun listenForIncomingPokes() {
        db.collection("pokes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("CarSNS", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val logBuilder = StringBuilder()
                    logBuilder.append("🟢 [실시간 채팅 & 콕찌르기 대화 내역]\n\n")

                    for (doc in snapshots.documents) {
                        val sender = doc.getString("sender") ?: "알 수 없음"
                        val receiver = doc.getString("receiver") ?: "알 수 없음"
                        val msg = doc.getString("message") ?: ""
                        val time = doc.getString("timeString") ?: ""
                        val status = doc.getString("vehicleStatus") ?: ""

                        logBuilder.append("• [$time] $sender ➔ $receiver: $msg (상태: $status)\n")
                    }

                    liveChatHistoryString = logBuilder.toString()
                }
            }
    }

    private fun getVehicleModelForFriend(friendName: String): String {
        return when {
            friendName.contains("김차차") -> "IONIQ 5"
            friendName.contains("김플레오맘") -> "PALISADE"
            friendName.contains("김플레오") -> "IONIQ 6"
            friendName.contains("이현대") -> "EV6"
            friendName.contains("성주은") -> "AVANTE N"
            friendName.contains("이예온") -> "SANTA FE"
            friendName.contains("엄성현") -> "TUCSON"
            friendName.contains("김국민") -> "CASPER"
            friendName.contains("최자두") -> "RAY EV"
            friendName.contains("최제네시스") -> "GENESIS G80"
            friendName.contains("한아이오닉") -> "IONIQ 5 N"
            else -> "IONIQ 5"
        }
    }

    private fun getAvatarResForFriend(friendName: String): Int {
        return when {
            friendName.contains("김플레오맘") -> R.drawable.icon_friend_324976
            friendName.contains("김플레오") || friendName.contains("김국민") -> R.drawable.icon_friend_686a72
            friendName.contains("이현대") || friendName.contains("최자두") || friendName.contains("최제네시스") -> R.drawable.icon_friend_324976
            friendName.contains("엄성현") || friendName.contains("한아이오닉") -> R.drawable.icon_friend_9ba8e2
            friendName.contains("성주은") || friendName.contains("이예온") -> R.drawable.icon_friend_c7c9ce
            friendName.contains("김차차") || friendName.contains("User") || friendName.contains("내 차") -> R.drawable.icon_friend_324976
            else -> R.drawable.icon_friend_324976
        }
    }

    private fun getNavMapResForFriend(friendName: String): Int {
        return when {
            friendName.contains("김플레오") -> R.drawable.nav_map_pleo
            friendName.contains("이현대") -> R.drawable.nav_map_hyundai
            friendName.contains("성주은") -> R.drawable.nav_map_jueun
            friendName.contains("이예온") -> R.drawable.nav_map_yeon
            friendName.contains("김국민") -> R.drawable.nav_map_kookmin
            else -> R.drawable.nav_map_pleo
        }
    }

    override fun onDestroy() {
        try {
            speechToText.release()
        } catch (e: Exception) {
            Log.e("STT", "SpeechToText release error: ${e.message}")
        }
        try {
            naviHelper.release()
        } catch (e: Exception) {
            Log.e("NaviHelper", "NaviHelper release error: ${e.message}")
        }
        super.onDestroy()
        vehicle.release()
    }
}