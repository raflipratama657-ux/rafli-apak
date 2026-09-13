package com.example.prayer

data class City(
    val name: String,
    val province: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneOffsetHours: Double = 7.0 // WIB = 7.0, WITA = 8.0, WIT = 9.0
)

object IndonesianCities {
    val CITIES = listOf(
        City("Jakarta", "DKI Jakarta", -6.2088, 106.8456, 7.0),
        City("Bandung", "Jawa Barat", -6.9175, 107.6191, 7.0),
        City("Surabaya", "Jawa Timur", -7.2575, 112.7521, 7.0),
        City("Semarang", "Jawa Tengah", -6.9667, 110.4167, 7.0),
        City("Yogyakarta", "D.I. Yogyakarta", -7.7956, 110.3695, 7.0),
        City("Serang", "Banten", -6.1200, 106.1503, 7.0),
        City("Medan", "Sumatera Utara", 3.5952, 98.6722, 7.0),
        City("Padang", "Sumatera Barat", -0.9471, 100.4172, 7.0),
        City("Palembang", "Sumatera Selatan", -2.9909, 104.7566, 7.0),
        City("Banda Aceh", "Aceh", 5.5483, 95.3238, 7.0),
        City("Pekanbaru", "Riau", 0.5071, 101.4478, 7.0),
        City("Bandar Lampung", "Lampung", -5.4500, 105.2667, 7.0),
        City("Pontianak", "Kalimantan Barat", -0.0263, 109.3425, 7.0),
        City("Banjarmasin", "Kalimantan Selatan", -3.3167, 114.5900, 8.0),
        City("Samarinda", "Kalimantan Timur", -0.5022, 117.1536, 8.0),
        City("Makassar", "Sulawesi Selatan", -5.1477, 119.4327, 8.0),
        City("Manado", "Sulawesi Utara", 1.4748, 124.8421, 8.0),
        City("Denpasar", "Bali", -8.6705, 115.2126, 8.0),
        City("Mataram", "Nusa Tenggara Barat", -8.5833, 116.1167, 8.0),
        City("Kupang", "Nusa Tenggara Timur", -10.1772, 123.6070, 8.0),
        City("Ambon", "Maluku", -3.6554, 128.1908, 9.0),
        City("Jayapura", "Papua", -2.5916, 140.6690, 9.0)
    )

    fun findCityByName(name: String): City {
        return CITIES.find { it.name.equals(name, ignoreCase = true) } ?: CITIES.first()
    }
}
