package co.kr.imokapp.headream

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform