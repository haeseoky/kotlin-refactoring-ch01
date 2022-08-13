package me.ocean.ch01

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.math.floor
import kotlin.math.max

class Refactoring {

    val playsTestData = """
        {
            "hamlet": { "name": "hamlet", "type": "tragedy" },
            "asLike": { "name": "asLike", "type": "comedy" },
            "othello": { "name": "othello", "type": "tragedy" }
        }
    """.trimIndent()


    val playsData = """
        [
            { "name": "hamlet", "type": "tragedy" },
            { "name": "asLike", "type": "comedy" },
            { "name": "othello", "type": "tragedy" }
        ]
    """.trimIndent()

    private val invoicesData = """
        
          {
            "customer": "BigCo",
            "performances": [
              {
                "playID": "hamlet",
                "audience": 55
              },
              {
                "playID": "asLike",
                "audience": 35
              },
              {
                "playID": "othello",
                "audience": 40
              }
            ]
          }
        
    """.trimIndent()

    val plays: List<Play> = jacksonObjectMapper().readValue(playsData)
    val invoice: Invoice = jacksonObjectMapper().readValue(invoicesData)

    @Test
    fun main() {


        println(plays)
        println(invoice)
        println(playsData)

        println(statements(invoice, plays))

        val expected = "청구내역(고객명: BigCo65000: 650 usd (55)58000: 580 usd (35)50000: 500 usd (40)총액: 1730 usd적립 포인트: 10"
        assertThat(statements(invoice, plays)).isEqualTo(expected)
    }

    private fun statements(invoices: Invoice, plays: List<Play>): String {
        var totalAmount = 0
        var result: String = "청구내역(고객명: ${invoices.customer}"

        for (performance in invoices.performances) {
            result += "${amountFor(performance)}: ${usd(amountFor(performance))} (${performance.audience})"
        }

        result += "총액: ${usd(totalAmount())}"
        result += "적립 포인트: ${totalVolumeCredits()}"
        return result
    }

    private fun totalAmount(): Int {
        var totalAmount = 0
        for (performance in invoice.performances) {
            totalAmount += amountFor(performance)
        }
        return totalAmount
    }

    private fun totalVolumeCredits(): Int {
        var volumeCredits = 0
        for (performance in invoice.performances) {
            volumeCredits = volumeCreditsFor(performance)
        }
        return volumeCredits
    }

    private fun usd(amount: Int): String = "${amount/100} usd"

    private fun volumeCreditsFor(
        performance: Performance
    ): Int {
        var volumeCredits = 0
        volumeCredits += max(performance.audience - 30, 0)
        if ("comedy" == playFor(performance).type) {
            volumeCredits += floor(performance.audience / 5.0).toInt()
        }
        return volumeCredits
    }

    private fun playFor(
        performance: Performance
    ) = plays.find { it.name == performance.playID } ?: throw IllegalArgumentException("없는 플래그")

    private fun amountFor(
        performance: Performance
    ): Int {
        var result = 0
        when (playFor(performance).type) {
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

data class PlaysData(val hamlet: Play, val asLike: Play, val othello: Play)