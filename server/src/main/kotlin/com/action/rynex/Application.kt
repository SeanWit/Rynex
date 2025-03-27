package com.action.rynex

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.management.ManagementFactory
import com.sun.management.OperatingSystemMXBean
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*

@Serializable
data class SystemInfo(
    val cpuUsage: Double,
    val memoryTotal: Long,
    val memoryFree: Long,
    val memoryUsed: Long,
    val diskTotal: Long,
    val diskFree: Long,
    val diskUsed: Long
)

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    
    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        
        get("/system-info") {
            val systemInfo = getSystemInfo()
            call.respond(systemInfo)
        }
    }
}

fun getSystemInfo(): SystemInfo {
    val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
    
    // CPU使用率
    val cpuUsage = osBean.cpuLoad * 100
    
    // 内存信息
    val memoryTotal = osBean.totalMemorySize
    val memoryFree = osBean.freeMemorySize
    val memoryUsed = memoryTotal - memoryFree
    
    // 获取磁盘信息
    val diskInfo = getDiskInfo()
    
    return SystemInfo(
        cpuUsage = cpuUsage,
        memoryTotal = memoryTotal,
        memoryFree = memoryFree,
        memoryUsed = memoryUsed,
        diskTotal = diskInfo.first,
        diskFree = diskInfo.second,
        diskUsed = diskInfo.first - diskInfo.second
    )
}

// 获取磁盘信息
private fun getDiskInfo(): Pair<Long, Long> {
    val process = Runtime.getRuntime().exec("df -k /")
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    reader.readLine() // 跳过标题行
    
    val line = reader.readLine()
    val parts = line.split("\\s+".toRegex())
    
    // 获取总空间和可用空间（转换为字节）
    val total = parts[1].toLong() * 1024
    val free = parts[3].toLong() * 1024
    
    return Pair(total, free)
}