package com.example.ParkingS.service.currency

import java.math.{BigDecimal, RoundingMode}

object CurrencyExchanger {
    def exchange(value: BigDecimal, sourceRate: BigDecimal, targetRate: BigDecimal): BigDecimal = {
        val result =
            if (sourceRate.equals(targetRate)) {
                value
            } else if (new BigDecimal("1").equals(sourceRate)) {
                value.divide(targetRate, RoundingMode.HALF_UP)
            } else if (new BigDecimal("1").equals(targetRate)) {
                value.multiply(sourceRate)
            } else {
                value.multiply(sourceRate).divide(targetRate, RoundingMode.HALF_UP)
            }
        
        result.setScale(2, BigDecimal.ROUND_HALF_UP)
    }
}
