package com.denizk0461.weserplaner.viewmodel

import android.app.Application
import com.denizk0461.weserplaner.model.SettingsPreferences
import com.denizk0461.weserplaner.model.StudIPEvent

class ScheduleUpdateViewModel(application: Application) : AppViewModel(application) {

    /**
     * Inserts a new schedule element.
     *
     * @param event the event to save
     */
    fun insert(event: StudIPEvent) {
        // Inser the event
        doAsync { repo.insert(event) }

        // Set that the user has modified their schedule
        repo.setPreference(SettingsPreferences.HAS_MODIFIED_SCHEDULE, true)
    }

    /**
     * Updates a schedule element.
     *
     * @param event the event to update
     */
    fun update(event: StudIPEvent) { doAsync { repo.update(event) } }

    /**
     * Deletes a schedule element.
     *
     * @param event the event to delete
     */
    fun delete(event: StudIPEvent) { doAsync { repo.delete(event) } }
}