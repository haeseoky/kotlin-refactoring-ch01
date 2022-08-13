package me.ocean.ch01

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Ch01Application

fun main(args: Array<String>) {
    runApplication<Ch01Application>(*args)
}
