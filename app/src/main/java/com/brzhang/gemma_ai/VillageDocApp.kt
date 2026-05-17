package com.brzhang.gemma_ai

import android.app.Application
import com.brzhang.gemma_ai.service.WeeklyReportWorker

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
