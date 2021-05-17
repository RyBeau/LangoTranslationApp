package nz.ac.uclive.oam23.tbc

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun toDate(dateString: String?): LocalDate? {
        return if (dateString == null) {
            null;
        } else {
            LocalDate.parse(dateString);
        }
    }

    @TypeConverter
    fun toDateString(date: LocalDate): String? {
        return date.toString();
    }

    @TypeConverter
    fun toLatLng(latLngString: String?): LatLng? {
        return if (latLngString == null) {
            null;
        } else {
            val latlong = latLngString.split(",".toRegex()).toTypedArray()
            val latitude = latlong[0].toDouble()
            val longitude = latlong[1].toDouble()
            LatLng(latitude, longitude)
        }
    }

    @TypeConverter
    fun toLatLngString(latLng: LatLng): String? {
        return latLng.latitude.toString() + ", " + latLng.longitude.toString()
    }
}