package com.feragusper.smokeanalytics.libraries.wear.data

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint

/**
 * Encodes/decodes a location into the Wear message payload (`MessageClient.sendMessage` data).
 * Smokes are added from the watch tile, which runs in the phone's background where the phone
 * can't get a location fix — so the watch captures its own GPS and ships the coordinates here.
 *
 * Format: `"lat;lng"` UTF-8. Null/blank means no location was available on the watch.
 */
object WearLocationCodec {

    fun encode(latitude: Double, longitude: Double): ByteArray =
        "$latitude;$longitude".encodeToByteArray()

    fun decode(data: ByteArray?): GeoPoint? {
        val raw = data?.decodeToString()?.trim().orEmpty()
        if (raw.isEmpty()) return null
        val parts = raw.split(";")
        if (parts.size != 2) return null
        val lat = parts[0].toDoubleOrNull() ?: return null
        val lng = parts[1].toDoubleOrNull() ?: return null
        return GeoPoint(latitude = lat, longitude = lng)
    }
}
