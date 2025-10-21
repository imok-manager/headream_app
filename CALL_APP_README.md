# 통화 앱 (Call Recording App)

Kotlin Multiplatform과 Compose Multiplatform을 사용하여 구현된 크로스플랫폼 통화 앱입니다.

## 🚀 주요 기능

### ✅ 구현된 기능
- **전화 걸기**: 모든 플랫폼에서 전화번호로 통화 시작
- **통화 녹음**: Android에서 통화 내용 자동 녹음
- **서버 업로드**: 녹음된 통화 내용을 서버로 자동 전송
- **통화 히스토리**: 전화번호별 통화 기록 및 요약 조회
- **권한 관리**: Android에서 필요한 권한 자동 요청

### 📱 플랫폼별 지원 기능

| 기능 | Android | iOS | Desktop | Web |
|------|---------|-----|---------|-----|
| 전화 걸기 | ✅ | ✅ | ⚠️ | ⚠️ |
| 통화 녹음 | ✅ | ❌ | ❌ | ❌ |
| 히스토리 조회 | ✅ | ✅ | ✅ | ✅ |
| 서버 통신 | ✅ | ✅ | ✅ | ✅ |

- ✅ 완전 지원
- ⚠️ 제한적 지원 (시스템 앱 연동)
- ❌ 미지원 (플랫폼 제약)

## 🏗️ 아키텍처

### 프로젝트 구조
```
composeApp/src/
├── commonMain/kotlin/org/example/project/
│   ├── data/           # 데이터 모델 (CallRecord, CallHistory 등)
│   ├── network/        # API 클라이언트 (Ktor 기반)
│   ├── phone/          # 전화 관리 인터페이스
│   ├── viewmodel/      # UI 상태 관리 (CallViewModel)
│   ├── ui/             # Compose UI (CallScreen)
│   └── di/             # 의존성 주입 모듈
├── androidMain/kotlin/org/example/project/
│   ├── phone/          # Android 전화/녹음 구현
│   └── di/             # Android 의존성 모듈
├── iosMain/kotlin/org/example/project/
│   ├── phone/          # iOS 전화 구현
│   └── di/             # iOS 의존성 모듈
└── [other platforms]/
```

### 핵심 컴포넌트

#### 1. PhoneManager (expect/actual 패턴)
```kotlin
interface PhoneManager {
    suspend fun makeCall(phoneNumber: String): Result<Unit>
    suspend fun startRecording(): Result<Unit>
    suspend fun stopRecording(): Result<ByteArray?>
    fun isRecordingSupported(): Boolean
}
```

#### 2. ApiClient (Ktor 기반)
```kotlin
class ApiClient {
    suspend fun uploadCallRecording(request: UploadCallRequest): Result<CallRecord>
    suspend fun getCallHistory(phoneNumber: String): Result<CallHistory>
    suspend fun getAllCallHistory(): Result<List<CallHistory>>
}
```

#### 3. CallViewModel (상태 관리)
- 전화 걸기/끊기 로직
- 녹음 시작/중지 관리
- 서버 통신 처리
- UI 상태 업데이트

## 🔧 설정 및 실행

### 1. 서버 설정
`ApiClient.kt`에서 서버 URL을 실제 API 서버 주소로 변경:
```kotlin
private val baseUrl = "https://your-api-server.com/api"
```

### 2. Android 빌드 및 실행
```bash
./gradlew :composeApp:assembleDebug
```

### 3. Desktop 빌드 및 실행
```bash
./gradlew :composeApp:run
```

### 4. iOS 빌드
Xcode에서 `iosApp` 디렉토리 열기 후 빌드

## 📋 필요한 권한 (Android)

앱에서 자동으로 요청하는 권한들:
- `CALL_PHONE`: 전화 걸기
- `RECORD_AUDIO`: 통화 녹음
- `READ_PHONE_STATE`: 통화 상태 확인
- `WRITE_EXTERNAL_STORAGE`: 녹음 파일 저장
- `INTERNET`: 서버 통신

## 🔒 보안 고려사항

### 통화 녹음 제한
- **Android**: API 29+ 에서 통화 녹음이 제한됨
- **iOS**: 시스템 정책상 통화 녹음 불가
- **실제 배포 시**: 각 국가의 통화 녹음 관련 법규 준수 필요

### 데이터 보안
- 녹음 파일은 임시 저장 후 서버 전송 완료 시 삭제
- HTTPS 통신 권장
- 서버에서 적절한 암호화 및 접근 제어 구현 필요

## 🚧 알려진 제한사항

1. **Android 통화 녹음**: 최신 Android 버전에서 제한적
2. **iOS 통화 녹음**: 플랫폼 정책상 불가능
3. **Web/Desktop 전화**: 시스템 앱 연동에 의존
4. **서버 구현**: 별도 백엔드 서버 구현 필요

## 🔄 향후 개선사항

- [ ] 로컬 데이터베이스 연동 (SQLDelight)
- [ ] 통화 시간 자동 계산
- [ ] 음성-텍스트 변환 (STT) 통합
- [ ] 푸시 알림 지원
- [ ] 통화 품질 분석
- [ ] 다국어 지원

## 📞 사용법

1. **전화번호 입력**: 메인 화면에서 전화번호 입력
2. **통화 시작**: "전화 걸기" 버튼 클릭
3. **자동 녹음**: Android에서 통화 연결 시 자동 녹음 시작
4. **통화 종료**: "통화 종료" 버튼으로 통화 및 녹음 종료
5. **히스토리 확인**: 하단에서 통화 기록 및 요약 확인

---

**주의**: 이 앱은 데모 목적으로 제작되었습니다. 실제 서비스 배포 시에는 각 국가의 통화 녹음 관련 법규를 반드시 확인하고 준수해야 합니다.
