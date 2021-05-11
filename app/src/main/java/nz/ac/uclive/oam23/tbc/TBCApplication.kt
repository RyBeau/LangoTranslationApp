package nz.ac.uclive.oam23.tbc

import android.app.Application

class TBCApplication: Application() {
    val database by lazy { TranslationDatabase.getDatabase(this)}
    val repository by lazy { TranslationRepository(database.translationDao()) }
}