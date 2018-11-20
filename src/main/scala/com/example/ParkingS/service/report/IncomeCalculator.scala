package com.example.ParkingS.service.report

import java.math.BigDecimal
import java.util

import com.example.ParkingS.model.{Currency, Ticket}
import com.example.ParkingS.service.currency.CurrencyExchanger

import scala.collection.JavaConverters

object IncomeCalculator {
    def calculate(tickets: util.List[Ticket], currency: Currency): BigDecimal = {
        JavaConverters.asScalaBuffer(tickets).foldLeft(new BigDecimal("0"))((sum, ticket) => {
            val current = CurrencyExchanger.exchange(ticket.value, ticket.currency.rate, currency.rate)
            sum add current
        })
    }
}
