package com.elderlyassist

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform