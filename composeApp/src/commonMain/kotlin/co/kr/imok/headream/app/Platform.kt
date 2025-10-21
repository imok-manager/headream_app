package co.kr.imok.headream.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform