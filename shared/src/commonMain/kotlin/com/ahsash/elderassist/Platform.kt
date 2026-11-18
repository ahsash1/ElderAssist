package com.ahsash.elderassist

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform