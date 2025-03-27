package com.action.rynex

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform