// app/src/main/java/com/oscaribarra/mealrotator/database/ScheduleDao.kt
package com.oscaribarra.mealrotator.database

import androidx.room.*
import com.oscaribarra.mealrotator.models.Schedule
import com.oscaribarra.mealrotator.models.ScheduleWithMeal

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule)

    @Query("SELECT * FROM schedules WHERE weekStartDate = :weekStartDate")
    suspend fun getWeekSchedule(weekStartDate: Long): List<ScheduleWithMeal>

    @Query("DELETE FROM schedules WHERE weekStartDate = :weekStart AND dayOfWeek = :dayOfWeek")
    suspend fun deleteDaySchedule(weekStart: Long, dayOfWeek: Int)

    @Query("UPDATE schedules SET isPinned = :isPinned WHERE id = :scheduleId")
    suspend fun updatePinnedStatus(scheduleId: Long, isPinned: Boolean)

    @Query("DELETE FROM schedules WHERE mealId = :mealId")
    suspend fun deleteSchedulesForMeal(mealId: Long)

}