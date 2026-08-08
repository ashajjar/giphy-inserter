package me.ahmadhajjar.giphy.config

import java.util.prefs.Preferences

object ConfigService {
    private val prefs = Preferences.userNodeForPackage(ConfigService::class.java)
    private const val API_KEY_PROP = "giphy_api_key"

    var apiKey: String
        get() = prefs.get(API_KEY_PROP, "")
        set(value) {
            prefs.put(API_KEY_PROP, value)
        }
}
