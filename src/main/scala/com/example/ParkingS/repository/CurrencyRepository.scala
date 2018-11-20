//package com.example.ParkingS.repository
//
//import java.lang.Long
//
//import com.example.ParkingS.model.Currency
//import org.springframework.data.jpa.repository.Query
//import org.springframework.data.repository.CrudRepository
//import org.springframework.data.repository.query.Param
//
//trait CurrencyRepository extends CrudRepository[Currency, Long] {
//    @Query("select c from Currency c where code = :code")
//    def findByCode(@Param("code") code: String): Currency
//}
