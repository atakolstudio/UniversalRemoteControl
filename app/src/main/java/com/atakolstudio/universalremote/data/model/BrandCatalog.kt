package com.atakolstudio.universalremote.data.model

import com.atakolstudio.universalremote.data.local.entity.DeviceCategory

/** Static fallback list shown instantly in the UI before the DB-backed brand query resolves. */
object BrandCatalog {
    val tvBrands = listOf(
        "Samsung", "LG", "Sony", "Vestel", "Arçelik", "Xiaomi", "Philips", "Panasonic",
        "Toshiba", "Hisense", "TCL", "Sharp", "JVC", "Grundig", "Beko", "Regal",
        "Telefunken", "Sunny", "Awox", "Hitachi", "Loewe", "Thomson", "Nokia", "Realme"
    )

    val acBrands = listOf(
        "Samsung", "LG", "Daikin", "Mitsubishi Electric", "Vestel", "Arçelik",
        "Mundoklima", "Panasonic", "Toshiba", "Hisense", "TCL", "Sharp", "Gree",
        "Midea", "Beko", "Airfel", "Baymak", "Fujitsu", "Carrier", "Haier"
    )

    val genericBrands = listOf(
        "Samsung", "LG", "Xiaomi", "Vestel", "Arçelik", "Sony", "Philips", "TCL", "Diğer"
    )

    fun brandsFor(category: DeviceCategory): List<String> = when (category) {
        DeviceCategory.TV -> tvBrands
        DeviceCategory.AIR_CONDITIONER -> acBrands
        else -> genericBrands
    }
}
