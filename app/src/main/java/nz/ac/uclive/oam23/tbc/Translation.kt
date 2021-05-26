package nz.ac.uclive.oam23.tbc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import java.util.*

@Entity(tableName = "translation")
class Translation(
    @ColumnInfo(name = "original_text") var originalText: String,
    @ColumnInfo(name = "translated_text") var translatedText: String,
    @ColumnInfo var date: LocalDate,
    @ColumnInfo var locationString: String,
    @ColumnInfo var locationLatLng: LatLng,
    @ColumnInfo var note: String
) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0

    override fun toString() = date.toString() + ": " + originalText
}