package nz.ac.uclive.oam23.tbc

import androidx.annotation.WorkerThread
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

    @Query("SELECT * FROM translation")
    fun getAll(): Flow<List<Translation>>
}

class TranslationRepository(private val translationDao: TranslationDao) {
    val translations: Flow<List<Translation>> = translationDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(translation: Translation) {
        translationDao.insert(translation)
    }
}
