package ro.razvanz.taxiapp.repository

import androidx.lifecycle.LiveData
import ro.razvanz.taxiapp.model.Taxi

class TaxiRepository(private val taxiDAO: TaxiDAO) {
    val taxis: LiveData<List<Taxi>> = taxiDAO.getAll()

    suspend fun add(taxi: Taxi) {
        taxiDAO.insert(taxi)
    }

    suspend fun delete(id: Int) {
        taxiDAO.delete(id)
    }

//    suspend fun delete(id: Int) {
//        taxiDAO.delete(id)
//    }
//
//    suspend fun update(taxi: Taxi) {
//        taxiDAO.update(taxi)
//        taxiDAO.insert(Taxi("", "", "", 0))
//        taxiDAO.delete(0)
//    }
//
//    suspend fun deleteAll() {
//        taxiDAO.deleteAll()
//    }
//
//    suspend fun syncAll() {
//        taxiDAO.syncAll()
//    }
}