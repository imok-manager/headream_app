package co.kr.imokapp.headream.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import co.kr.imokapp.headream.data.CallHistory
import co.kr.imokapp.headream.data.CallRecord
import co.kr.imokapp.headream.data.CallStatus
import co.kr.imokapp.headream.viewmodel.CallViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    viewModel: CallViewModel,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val callHistories = viewModel.callHistories
    
    // 에러 메시지 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            // 실제로는 SnackBar 등으로 표시
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 통화 버튼
        CallInputSection(
            phoneNumber = uiState.phoneNumber,
            onMakeCall = viewModel::makeCall,
            onEndCall = viewModel::endCall,
            isLoading = uiState.isLoading,
            isRecording = uiState.isRecording,
            callStatus = uiState.callStatus
        )
        
        // 통화 히스토리
        CallHistorySection(
            callHistories = callHistories,
            isLoading = uiState.isLoadingHistory,
            onRefresh = { viewModel.loadCallHistory() }
        )
    }
    
    // 에러 다이얼로그
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("오류") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("확인")
                }
            }
        )
    }
}

@Composable
private fun CallInputSection(
    phoneNumber: String,
    onMakeCall: () -> Unit,
    onEndCall: () -> Unit,
    isLoading: Boolean,
    isRecording: Boolean,
    callStatus: CallStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "통화하기",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMakeCall,
                    enabled = !isLoading && callStatus == CallStatus.COMPLETED,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Call, contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("전화 걸기")
                }
                
                if (callStatus != CallStatus.COMPLETED) {
                    Button(
                        onClick = onEndCall,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("통화 종료")
                    }
                }
            }
            
            // 통화 상태 표시
            CallStatusIndicator(
                callStatus = callStatus,
                isRecording = isRecording
            )
        }
    }
}

@Composable
private fun CallStatusIndicator(
    callStatus: CallStatus,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    if (callStatus != CallStatus.COMPLETED) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (callStatus) {
                    CallStatus.DIALING -> MaterialTheme.colorScheme.primaryContainer
                    CallStatus.CONNECTED -> MaterialTheme.colorScheme.secondaryContainer
                    CallStatus.RECORDING -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (callStatus) {
                        CallStatus.DIALING -> Icons.Default.Phone
                        CallStatus.CONNECTED -> Icons.Default.Call
                        CallStatus.RECORDING -> Icons.Default.Mic
                        else -> Icons.Default.CallEnd
                    },
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when (callStatus) {
                        CallStatus.DIALING -> "전화 연결 중..."
                        CallStatus.CONNECTED -> "통화 중"
                        CallStatus.RECORDING -> "통화 중 (녹음 중)"
                        CallStatus.FAILED -> "통화 실패"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (isRecording) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.FiberManualRecord,
                        contentDescription = "녹음 중",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CallHistorySection(
    callHistories: List<CallHistory>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "통화 히스토리",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onRefresh) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            if (callHistories.isEmpty() && !isLoading) {
                Text(
                    text = "통화 기록이 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(callHistories) { history ->
                        CallHistoryItem(history = history)
                    }
                }
            }
        }
    }
}

@Composable
private fun CallHistoryItem(
    history: CallHistory,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = history.phoneNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${history.totalCalls}회 통화",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            history.lastCallTime?.let { lastCall ->
                Text(
                    text = "마지막 통화: ${lastCall.toLocalDateTime(TimeZone.currentSystemDefault())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 최근 통화 요약 표시
            history.calls.firstOrNull()?.summary?.let { summary ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "요약: $summary",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
