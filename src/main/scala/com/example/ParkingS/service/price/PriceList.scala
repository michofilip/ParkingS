package com.example.ParkingS.service.price

import java.math.BigDecimal

class PriceList(private val constants: IndexedSeq[BigDecimal],
                private val multiplier: BigDecimal,
                val currencyCode: String) {
    
    def getPrice(hours: Int): BigDecimal = {
        def p(h: Int, sum: BigDecimal, current: BigDecimal): BigDecimal = {
            if (h <= hours) {
                val curr =
                    if (h <= constants.length) {
                        constants(h - 1)
                    } else {
                        current multiply multiplier
                    }
                p(h + 1, sum add curr, curr)
            } else {
                sum
            }
        }
        
        p(1, new BigDecimal("0"), new BigDecimal("0"))
    }
}

object PriceList {
    def getRegularPriceListPLN: PriceList = {
        val constants = IndexedSeq(new BigDecimal("1"), new BigDecimal("2"))
        val multiplier = new BigDecimal("1.5")
        val currencyCode = "PLN"
        new PriceList(constants, multiplier, currencyCode)
    }
    
    def getDisabledPriceListPLN: PriceList = {
        val constants = IndexedSeq(new BigDecimal("0"), new BigDecimal("2"))
        val multiplier = new BigDecimal("1.2")
        val currencyCode = "PLN"
        new PriceList(constants, multiplier, currencyCode)
    }
}
