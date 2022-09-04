package me.ocean.ch01

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.math.floor

class Refactoring {

    val playsData = """
        {
            "Hamlet": { "name": "Hamlet", "type": "tragedy" },
            "as-like": { "name": "as-like", "type": "comedy" },
            "Othello": { "name": "Othello", "type": "tragedy" }
        }
    """.trimIndent()

    var plays = jacksonObjectMapper().readValue<Map<String, Play>>(playsData)

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

    var invoices = jacksonObjectMapper().readValue(invoicesData, Invoice::class.java)

    @Test
    fun main() {

        println(statements(invoices, plays))

        val expected = "청구내역(고객명: BigCo)\n" +
                "Hamlet, \$650, (55석)\n" +
                "as-like, \$580, (35석)\n" +
                "Othello, \$500, (40석)\n" +
                "총액: \$1730\n" +
                "적립 포인트: 47"
        assertThat(statements(invoices, plays)).isEqualTo(expected)
    }

    private fun statements(invoices: Invoice, plays: Map<String, Play>): String {

        return renderPlainText(createStatementData(invoices, plays))
    }

    private fun createStatementData(invoices: Invoice, plays: Map<String, Play>): StatementData {

        return StatementData(
            customer = invoices.customer,
            performances = invoices.performances,
            totalAmount = totalAmount(),
            totalVolumeCredits = totalVolumeCredits()
        )
    }

    private fun renderPlainText(data: StatementData): String {
        var result: String = "청구내역(고객명: ${data.customer})\n"

        for (performance in data.performances) {
            //청구내역을 출력한다.
            result += "${playFor(performance).name}, ${usd(amountFor(performance))}, (${performance.audience}석)\n"
        }

        result += "총액: ${usd(data.totalAmount)}\n"
        result += "적립 포인트: ${data.totalVolumeCredits}"
        return result
    }

    private fun totalAmount(): Int {
        var result = 0
        for (performance in invoices.performances) {
            //청구내역을 출력한다.
            result += amountFor(performance)
        }
        return result
    }

    private fun totalVolumeCredits(): Int {
        var result = 0
        for (performance in invoices.performances) {
            result += volumeCreditsFor(performance)

        }
        return result
    }

    private fun usd(totalAmount: Int) = "$${totalAmount / 100}"

    private fun volumeCreditsFor(
        performance: Performance
    ): Int {
        //포인트 적립
        var volumeCredits = 0
        volumeCredits += Math.max(performance.audience - 30, 0)

        if ("comedy" == playFor(performance).type) volumeCredits += floor(performance.audience / 5.0).toInt()
        return volumeCredits
    }

    private fun playFor(
        performance: Performance
    ) = plays[performance.playID]!!

    private fun amountFor(
        performance: Performance
    ): Int {
        val result = when (playFor(performance).type) {
            "tragedy" -> {
                PlayPerformanceCalculator().amountCalcurate(performance, play = playFor(performance), TragedyCalculator())
            }
            "comedy" -> {
                PlayPerformanceCalculator().amountCalcurate(performance, play = playFor(performance), ComedyCalculator())
            }
            else -> 0
        }
        return result
    }

}

class PlayPerformanceCalculator{
    fun amountCalcurate(performance: Performance, play: Play, performanceCalculator: PerformanceCalculator): Int {
        return performanceCalculator.amountCalculate( performance, play)
    }

    fun volumeCreditsCalcurate(performance: Performance, play: Play, performanceCalculator: PerformanceCalculator): Int {
        return performanceCalculator.volumeCreditsCalculate( performance)
    }
}

interface PerformanceCalculator {
    fun amountCalculate(performance: Performance, play: Play): Int
    fun volumeCreditsCalculate(performance: Performance): Int
}

class TragedyCalculator : PerformanceCalculator {
    override fun amountCalculate(performance: Performance, play: Play): Int {
        var result = 40000
        if (performance.audience > 30) {
            result += 1000 * (performance.audience - 30)
        }
        return result
    }

    override fun volumeCreditsCalculate(performance: Performance): Int {
        TODO("Not yet implemented")
    }
}

class ComedyCalculator : PerformanceCalculator {
    override fun amountCalculate(performance: Performance, play: Play): Int {
        var result = 30000
        if (performance.audience > 20) {
            result += 10000 + 500 * (performance.audience - 20)
        }
        result += 300 * performance.audience
        return result
    }

    override fun volumeCreditsCalculate(performance: Performance): Int {
        TODO("Not yet implemented")
    }
}



data class Invoice(var customer: String, var performances: List<Performance>)
data class Performance(val playID: String, val audience: Int)
data class Play(val name: String, val type: String)

data class StatementData(
    val customer: String,
    val performances: List<Performance>,
    var totalAmount: Int = 0,
    var totalVolumeCredits: Int = 0
)