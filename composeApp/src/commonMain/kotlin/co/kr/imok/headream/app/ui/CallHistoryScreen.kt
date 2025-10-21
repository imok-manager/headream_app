package co.kr.imok.headream.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.kr.imok.headream.app.data.CallRecord
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    callHistory: List<CallRecord>,
    isLoading: Boolean = false,
    isLoadingMore: Boolean = false,
    onNavigateBack: () -> Unit,
    onCallItemClick: (CallRecord) -> Unit,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    totalCount: Int = callHistory.size, // 전체 건수
    modifier: Modifier = Modifier
) {
    // Pull-to-refresh 상태
    val pullToRefreshState = rememberPullToRefreshState()
    // LazyColumn 스크롤 상태
    val listState = rememberLazyListState()
    
    // 화면 진입 시 데이터가 없을 때만 API 호출
    LaunchedEffect(Unit) {
        if (callHistory.isEmpty() && !isLoading) {
            println("📋 CallHistoryScreen - 데이터가 없어서 API 호출")
            onRefresh()
        } else {
            println("📋 CallHistoryScreen - 기존 데이터 사용 (${callHistory.size}개)")
        }
    }
    
    // 스크롤 끝 감지
    LaunchedEffect(listState, callHistory.size) {
        snapshotFlow { 
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val totalItems = layoutInfo.totalItemsCount
            lastVisibleIndex to totalItems
        }.collect { (lastVisibleIndex, totalItems) ->
            if (lastVisibleIndex != null && 
                totalItems > 0 &&
                lastVisibleIndex >= totalItems - 3 &&
                callHistory.isNotEmpty() && 
                !isLoading && 
                !isLoadingMore) {
                onLoadMore()
            }
        }
    }
    
    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { onRefresh() },
        state = pullToRefreshState,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(vertical = 16.dp)
        ) {
            // 상단 타이틀
            Text(
                text = "상담 기록",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                textAlign = TextAlign.Center
            )
            
            // 안내 메시지 박스
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x33F2B28C)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "상담 기록을 탭하면",
                        fontSize = 18.sp,
                        color = Color(0xFFF88541),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "녹음 파일 및 상세정보를 확인할 수 있습니다.",
                        fontSize = 18.sp,
                        color = Color(0xFFF88541),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // 상담 기록 리스트 또는 빈 상태 메시지
            if (callHistory.isEmpty() && !isLoading) {
                // 데이터가 없을 때 안내 메시지
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "상담기록이 없습니다",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "상담을 시작해보세요",
                            fontSize = 16.sp,
                            color = Color(0xFF999999),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // 상담 기록 리스트
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    itemsIndexed(callHistory) { index, record ->
                        CallHistoryItem(
                            callRecord = record,
                            index = maxOf(1, totalCount - index), // 최소값 1로 보장
                            onClick = { onCallItemClick(record) }
                        )
                    }
                    
                    // 로딩 중 표시
                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallHistoryItem(
    callRecord: CallRecord,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val recordDate = callRecord.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysAgo = recordDate.daysUntil(today)
    
    // DB에서 오는 값 그대로 사용
    val recordDateTime = callRecord.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    
    // 날짜 표시 형식: 오늘(시간), 어제, 또는 MM월 DD일
    val displayDate = when (daysAgo) {
        0 -> {
            // 오늘: 오전/오후 시간 표시
            val hour = recordDateTime.hour
            val minute = recordDateTime.minute
            val amPm = if (hour < 12) "오전" else "오후"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
"$amPm $displayHour:${minute.toString().padStart(2, '0')}"
        }
        1 -> "어제"
        else -> "${recordDate.monthNumber}월 ${recordDate.dayOfMonth}일" // 이전: 월/일 표시
    }
    
    // 녹음파일 상태
    val audioStatus = if (callRecord.audioFileUrl.isNullOrEmpty()) "녹음파일 준비중" else "녹음파일 있음"
    
    // 통화 시간 포맷팅 (N분 N초 또는 N초)
    val durationText = if (callRecord.duration >= 60) {
        val minutes = callRecord.duration / 60
        val seconds = callRecord.duration % 60
        "${minutes}분 ${seconds}초"
    } else {
        "${callRecord.duration}초"
    }
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 사람 아이콘 (요약 상태에 따라 색상 변경)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (callRecord.summary?.isNotEmpty() == true) 
                            Color(0xFF4CAF50) // 초록색 (요약 있음)
                        else 
                            Color(0xFF9E9E9E) // 회색 (요약 없음)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "상담사",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 내용 영역
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 상단: 제목과 날짜
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 제목 (N번째 통화)
                    Text(
                        text = "상담전화 #$index",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // 날짜
                    Text(
                        text = displayDate,
                        color = Color(0xFF666666),
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 통화 정보 (제목 바로 아래)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 통화 시간
                    Text(
                        text = durationText,
                        color = Color(0xFF212121),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // 구분점
                    Text(
                        text = " · ",
                        color = Color(0xFFBDBDBD),
                        fontSize = 14.sp
                    )
                    
                    // 녹음파일 상태
                    Text(
                        text = audioStatus,
                        color = if (callRecord.audioFileUrl.isNullOrEmpty()) Color(0xFF9E9E9E) else Color(0xFF2196F3),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // 요약 내용 (있는 경우에만 표시)
                if (callRecord.summary?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = callRecord.summary,
                        color = Color(0xFF666666),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
