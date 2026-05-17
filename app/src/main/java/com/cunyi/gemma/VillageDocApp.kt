package com.cunyi.gemma

import android.app.Application
import com.cunyi.gemma.service.WeeklyReportWorker

class VillageDocApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        WeeklyReportWorker.schedule(this)
    }

    companion object {
        lateinit var instance: VillageDocApp
            private set
    }
}
