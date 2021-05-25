package nz.ac.uclive.oam23.tbc

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Insert
    suspend fun insert(translation: Translation): Long

    @Update
    suspend fun update(translation: Translation)

    @Delete
    suspend fun delete(translation: Translation)

    @Query("SELECT * FROM translation WHERE :id = id")
    fun getOne(id: Long): Translation

    @Query("SELECT * FROM translation")
    fun getAll(): Flow<List<Translation>>

    @Query("SELECT * FROM translation WHERE id = :key")
    fun getTranslation(key: Long): Flow<Translation>
}

class TranslationRepository(private val translationDao: TranslationDao) {
    val translations: Flow<List<Translation>> = translationDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(translation: Translation) {
        translationDao.insert(translation)
    }

    fun getTranslation(key: Long): Flow<Translation>{
        return translationDao.getTranslation(key)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(translation: Translation) {
        translationDao.delete(translation)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(translation: Translation) {
        translationDao.update(translation)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getTranslationById(id: Long): Translation {
        return translationDao.getOne(id)
    }
}
