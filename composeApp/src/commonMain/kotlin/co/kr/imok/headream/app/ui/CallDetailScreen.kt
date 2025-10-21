package co.kr.imok.headream.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.kr.imok.headream.app.audio.AudioPlaybackManager
import co.kr.imok.headream.app.data.CallRecord
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallDetailScreen(
    callRecord: CallRecord?,
    callIndex: Int = 1, // 몇 번째 상담통화인지
    isAudioPlaying: Boolean = false, // 외부에서 관리되는 재생 상태
    onNavigateBack: () -> Unit,
    onPlayAudio: (String) -> Unit = {}, // 오디오 재생 콜백
    onLoadDetail: (String, (CallRecord?) -> Unit) -> Unit = { _, _ -> }, // 상세 데이터 로드 콜백
    modifier: Modifier = Modifier
) {
    var detailRecord by remember { mutableStateOf(callRecord) }
    
    // 화면 진입 시 상세 데이터 로드
    LaunchedEffect(callRecord?.id) {
        callRecord?.id?.let { callId ->
            println("📋 통화 상세 데이터 로드 시작: $callId")
            onLoadDetail(callId) { loadedRecord ->
                println("📋 통화 상세 데이터 로드 완료: ${loadedRecord != null}")
                detailRecord = loadedRecord ?: callRecord
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "${callIndex}번째 상담통화",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (detailRecord == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("통화 기록을 찾을 수 없습니다.")
            }
            return@Scaffold
        }
        
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            detailRecord?.let { currentRecord ->
                // 통화 정보 카드
                CallInfoCard(callRecord = currentRecord)
                
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                
                // 통화 요약 카드 (있는 경우에만)
                currentRecord.summary?.let { summary ->
                    if (summary.isNotEmpty()) {
                        CallSummaryCard(summary = summary)
                        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                    }
                }
                
                // 통화 전체 내용 카드 (있는 경우에만)
                val fullContent = currentRecord.callContent ?: currentRecord.transcription

                if (!fullContent.isNullOrEmpty()) {
                    CallContentCard(content = fullContent)
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                } else {
                    println("⚠️ 통화 전체 내용이 비어있습니다")
                }
                
                // 상담 녹음 듣기 버튼
                AudioPlayButton(
                    audioFilePath = currentRecord.audioFileUrl,
                    isPlaying = isAudioPlaying,
                    onPlayClick = { path ->
                        println("🎵 오디오 재생 버튼 클릭: $path")
                        onPlayAudio(path)
                    }
                )
            }
        }
    }
}

@Composable
private fun CallInfoCard(callRecord: CallRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp) // 모서리 제거
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 통화 시간 범위
            val startTime = formatDateTime(callRecord.timestamp)
            val endTime = if (callRecord.duration > 0) {
                val endInstant = callRecord.timestamp.plus(kotlin.time.Duration.parse("${callRecord.duration}s"))
                formatDateTime(endInstant)
            } else {
                "종료시간 미상"
            }
            
            Text(
                text = "$startTime ~ $endTime",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121)
            )
            
            // 상담 시간
            val durationText = formatDuration(callRecord.duration)
            Text(
                text = "상담시간 : $durationText",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun CallSummaryCard(summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp) // 모서리 제거
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "통화요약",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            
            Text(
                text = summary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF424242)
            )
        }
    }
}

@Composable
private fun CallContentCard(content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp) // 모서리 제거
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "통화전체 내용",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            
            Text(
                text = content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF424242)
            )
        }
    }
}

@Composable
private fun AudioPlayButton(
    audioFilePath: String?,
    isPlaying: Boolean,
    onPlayClick: (String) -> Unit
) {
    val isEnabled = !audioFilePath.isNullOrEmpty()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp) // 모서리 제거
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 재생 버튼
            Button(
                onClick = { 
                    if (isEnabled) {
                        onPlayClick(audioFilePath!!)
                    }
                },
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) Color(0xFFFF6B6B) else Color(0xFF4CAF50),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color(0xFF9E9E9E)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "일시정지" else "재생",
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Text(
                        text = if (isPlaying) "재생 중..." else "상담 녹음 듣기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 재생 컨트롤바 (재생 중일 때만 표시)
            if (isPlaying) {
                Spacer(modifier = Modifier.height(16.dp))
                AudioControlBar(
                    onStopClick = {
                        audioFilePath?.let { path ->
                            onPlayClick(path) // 재생 상태를 토글해서 정지
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AudioControlBar(
    onStopClick: () -> Unit = {}
) {
    // 실제 오디오 재생 상태 가져오기
    var currentPosition by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }
    var isCurrentlyPlaying by remember { mutableStateOf(false) }
    
    // 재생 시간 및 상태 업데이트
    LaunchedEffect(Unit) {
        while (true) {
            try {
                currentPosition = AudioPlaybackManager.getCurrentPosition()
                val duration = AudioPlaybackManager.getDuration()
                if (duration > 0) {
                    totalDuration = duration
                }
                isCurrentlyPlaying = AudioPlaybackManager.isPlaying()
                kotlinx.coroutines.delay(1000)
            } catch (e: Exception) {
                println("⚠️ 오디오 상태 업데이트 실패: ${e.message}")
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    Column {
        // 진행 바
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime((currentPosition / 1000).toInt()),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.width(40.dp)
            )
            
            Slider(
                value = if (totalDuration > 0) currentPosition.toFloat() else 0f,
                onValueChange = { newPosition ->
                    try {
                        AudioPlaybackManager.seekTo(newPosition.toLong())
                        println("🎯 Slider 위치 변경: ${newPosition.toLong()}ms")
                    } catch (e: Exception) {
                        println("❌ Slider 위치 변경 실패: ${e.message}")
                    }
                },
                valueRange = 0f..(if (totalDuration > 0) totalDuration.toFloat() else 1f),
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF4CAF50),
                    activeTrackColor = Color(0xFF4CAF50),
                    inactiveTrackColor = Color(0xFFE0E0E0)
                )
            )
            
            Text(
                text = formatTime((totalDuration / 1000).toInt()),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.width(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 컨트롤 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 되감기 버튼
            IconButton(
                onClick = { 
                    try {
                        val newPosition = maxOf(0L, currentPosition - 15000L) // 15초 = 15000ms
                        AudioPlaybackManager.seekTo(newPosition)
                        println("⏪ 15초 되감기: ${newPosition}ms")
                    } catch (e: Exception) {
                        println("❌ 되감기 실패: ${e.message}")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = "15초 되감기",
                    tint = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 일시정지/재생 버튼
            IconButton(
                onClick = { 
                    try {
                        if (isCurrentlyPlaying) {
                            AudioPlaybackManager.pause()
                            println("⏸️ 재생 일시정지")
                        } else {
                            AudioPlaybackManager.resume()
                            println("▶️ 재생 재개")
                        }
                    } catch (e: Exception) {
                        println("❌ 일시정지/재생 실패: ${e.message}")
                    }
                }
            ) {
                Icon(
                    imageVector = if (isCurrentlyPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isCurrentlyPlaying) "일시정지" else "재생",
                    tint = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 빨리감기 버튼
            IconButton(
                onClick = { 
                    try {
                        val newPosition = if (totalDuration > 0) {
                            minOf(totalDuration, currentPosition + 15000L) // 15초 = 15000ms
                        } else {
                            currentPosition + 15000L
                        }
                        AudioPlaybackManager.seekTo(newPosition)
                        println("⏩ 15초 빨리감기: ${newPosition}ms")
                    } catch (e: Exception) {
                        println("❌ 빨리감기 실패: ${e.message}")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "15초 빨리감기",
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes}:${remainingSeconds.toString().padStart(2, '0')}"
}

private fun formatDateTime(instant: Instant): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val year = dateTime.year
    val month = dateTime.monthNumber
    val day = dateTime.dayOfMonth
    val hour = dateTime.hour
    val minute = dateTime.minute
    val second = dateTime.second
    
    return "${year}년 ${month}월 ${day}일 ${hour.toString().padStart(2, '0')}시 ${minute.toString().padStart(2, '0')}분 ${second.toString().padStart(2, '0')}초"
}

private fun formatDuration(seconds: Long): String {
    return if (seconds >= 60) {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        "${minutes}분 ${remainingSeconds}초"
    } else {
        "${seconds}초"
    }
}
