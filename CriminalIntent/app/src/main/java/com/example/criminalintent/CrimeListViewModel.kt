package com.example.criminalintent

import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*

class CrimeListViewModel : ViewModel() {

    private val crimeRepository: CrimeRepository = CrimeRepository.getInstance()

    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime){
        crimeRepository.addCrime(crime)
    }

    fun removeCrime(crime: Crime) {
        crimeRepository.removeCrime(crime)
    }

}