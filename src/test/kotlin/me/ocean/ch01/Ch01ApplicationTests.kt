package me.ocean.ch01

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class Ch01ApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    fun test(){
        val playsData = """
        {
            "Hamlet": { "name": "Hamlet", "type": "tragedy" },
            "as-like": { "name": "as-like", "type": "comedy" },
            "Othello": { "name": "Othello", "type": "tragedy" }
        }
    """.trimIndent()

        var plays = ArrayList<Play>()
        val readValue = jacksonObjectMapper().readValue(playsData, Map::class.java)
        println(readValue)
        println(readValue["Hamlet"])
    }

}
