package com.example.ParkingS.repository

import java.math.BigDecimal

import com.example.ParkingS.model.Currency
import org.springframework.stereotype.Repository

@Repository
class CurrencyFakeRepository() {
    private var currencies: IndexedSeq[Currency] = {
        val currency = new Currency()
        currency.id = 1
        currency.name = "Polskie złote"
        currency.code = "PLN"
        currency.rate = new BigDecimal("1")
        IndexedSeq(currency)
    }
    
    def findOne(id: Long): Currency = {
        currencies.find(c => c.id == id) match {
            case Some(currency) => currency
            case None => null
        }
    }
    
    def exists(id: Long): Boolean = {
        currencies.exists(c => c.id == id)
    }
    
    def delete(id: Long): Unit = {
        currencies = currencies.filterNot(c => c.id == id)
    }
    
    def delete(currency: Currency): Unit = {
        delete(currency.id)
    }
    
    def save(currency: Currency): Unit = {
        if (currency.id == 0) {
            currency.id = nextId()
            currencies = currencies :+ currency
        } else {
            delete(currency)
            currencies = currencies :+ currency
        }
    }
    
    private def nextId(): Long = {
        def n(id: Long, cs: Seq[Currency]): Long = {
            cs match {
                case head +: tail =>
                    if (head.id >= id) {
                        n(head.id + 1, tail)
                    } else {
                        n(id, tail)
                    }
                case Nil => id
            }
        }
        
        n(1, currencies)
    }
    
    def findByCode(code: String): Currency = {
        currencies.find(c => c.code == code) match {
            case Some(currency) => currency
            case None => null
        }
    }
}
