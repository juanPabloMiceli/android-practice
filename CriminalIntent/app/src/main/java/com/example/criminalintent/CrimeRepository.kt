package com.example.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.criminalintent.database.CrimeDao
import com.example.criminalintent.database.CrimeDatabase
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) {

    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(CrimeDatabase.migration_1_2).build()

    private val crimeDao: CrimeDao = database.crimeDao()
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime){
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime){
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    fun removeCrime(crime: Crime) {
        executor.execute {
            crimeDao.removeCrime(crime)
        }
    }

    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)

    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun getInstance(): CrimeRepository{
            return INSTANCE ?: throw IllegalStateException("Crime repository must be initialized!")
        }
    }

}