package com.denizk0461.studip.model

/**
 * Enumeration class for user-set preferences that will be saved in persistent storage via
 * SharedPreferences.
 *
 * @param key   key for the SharedPreferences transaction
 */
enum class SettingsPreferences(val key: String) {

    /**
     * Which allergens the user wants displayed or hidden.
     */
    ALLERGEN_CONFIG("setting_allergen_config"),

    /**
     * Whether the user wants to have allergens displayed in the canteen overview.
     */
    ALLERGEN("setting_allergen"),

    /**
     * Whether the user wants their timetable on the current day.
     */
    CURRENT_DAY("settings_current_day"),

    /**
     * Whether the user wants the next course in his schedule to be highlighted.
     */
    COURSE_HIGHLIGHTING("setting_highlight"),

    /**
     * Whether the user wants the app to launch with the canteen fragment.
     */
    LAUNCH_CANTEEN_ON_START("settings_launch_canteen"),

    /**
     * Whether the user wants dietary preferences to be coloured.
     */
    COLOUR_PREFS("settings_prefs_colour"),

    /**
     * Whether the user opts into sending crash report.
     */
    DATA_HANDLING("setting_data_handling"),

    /**
     * The canteen the user has picked.
     */
    CANTEEN("setting_canteen"),
    ;
}