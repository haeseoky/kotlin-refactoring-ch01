package me.ocean.ch01

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.math.floor

class Refactoring {

    val playsTestData = """
        {
            "hamlet": { "name": "Hamlet", "type": "tragedy" },
            "asLike": { "name": "as-like", "type": "comedy" },
            "othello": { "name": "Othello", "type": "tragedy" }
        }
    """.trimIndent()


    val playsData = """
        [
            { "name": "Hamlet", "type": "tragedy" },
            { "name": "as-like", "type": "comedy" },
            { "name": "Othello", "type": "tragedy" }
        ]
    """.trimIndent()

    private val invoicesData = """
        
          {
            "customer": "BigCo",
            "performances": [
              {
                "playID": "Hamlet",
                "audience": 55
              },
              {
                "playID": "as-like",
                "audience": 35
              },
              {
                "playID": "Othello",
                "audience": 40
              }
            ]
          }
        
    """.trimIndent()

    @Test
    fun main(){
        val mapper = jacksonObjectMapper()
        val plays : List<Play> = mapper.readValue(playsData)

        val invoice = mapper.readValue(invoicesData, Invoice::class.java)

        println(statements(invoice, plays))

        val expected = "청구내역(고객명: BigCo)\n" +
                "Hamlet, \$650, (55석)\n" +
                "as-like, \$580, (35석)\n" +
                "Othello, \$500, (40석)\n" +
                "총액: \$1730\n" +
                "적립 포인트: 47"
        assertThat(statements(invoice, plays)).isEqualTo(expected)
    }

    private fun statements(invoices: Invoice, plays: List<Play>): String {
        var totalAmount = 0
        var volumeCredits = 0
        var result : String = "청구내역(고객명: ${invoices.customer})\n"

        for(performance in invoices.performances){
            val play = plays.find { it.name == performance.playID } ?: throw IllegalArgumentException("없는 플래그")
            var thisAmount = 0
            thisAmount = amountFor(play, performance)
            volumeCredits += Math.max(performance.audience - 30, 0)
            if ( "comedy" == play.type ) volumeCredits += floor(performance.audience / 5.0).toInt()
            totalAmount += thisAmount
            result += "${play.name}, $${thisAmount/100}, (${performance.audience}석)\n"

        }
        result += "총액: $${totalAmount/100}\n"
        result += "적립 포인트: $volumeCredits"
        return result
    }

    private fun amountFor(
        play: Play,
        performance: Performance
    ): Int {
        var result = 0
        when (play.type) {
            "tragedy" -> {
                result = 40000
                if (performance.audience > 30) {
                    result += 1000 * (performance.audience - 30)
                }
            }
            "comedy" -> {
                result = 30000
                if (performance.audience > 20) {
                    result += 10000 + 500 * (performance.audience - 20)
                }
                result += 300 * performance.audience
            }
        }
        return result
    }

}

data class Invoice(val customer: String, val performances: List<Performance>)
data class Performance(val playID: String, val audience: Int)
data class Play(val name: String, val type: String)
