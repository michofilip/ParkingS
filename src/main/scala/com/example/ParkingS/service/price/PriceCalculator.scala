package com.example.ParkingS.service.price

import java.math.BigDecimal
import java.time.Duration

import com.example.ParkingS.model.Ticket
import com.example.ParkingS.repository.CurrencyFakeRepository
import com.example.ParkingS.service.currency.CurrencyExchanger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PriceCalculator @Autowired()(currencyRepository: CurrencyFakeRepository) {
    def calculate(ticket: Ticket): BigDecimal = {
        val hours = calculateHours(ticket)
        val priceList =
            if (ticket.disabled) {
                PriceList.getDisabledPriceListPLN
            } else {
                PriceList.getRegularPriceListPLN
            }
        
        val priceListCurrencyRate = currencyRepository.findByCode(priceList.currencyCode).rate
        
        val valueOrg = priceList.getPrice(hours)
        val valueExc = CurrencyExchanger.exchange(valueOrg, priceListCurrencyRate, ticket.currency.rate)
        
        valueExc
    }
    
    private def calculateHours(ticket: Ticket): Int = {
        val between = Duration.between(ticket.begin, ticket.end)
        val sec = between.toMillis / 1000
        Math.ceil(sec / 3600.0).toInt
    }
}
