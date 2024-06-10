import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import it.progmob.passwordmanager.Notification

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        // Perform the background task here
        performBackgroundTask()
        return Result.success()
    }
    @SuppressLint("ScheduleExactAlarm")
    private fun performBackgroundTask() {
        val notificationID = System.currentTimeMillis().toInt()

        val intent = Intent(applicationContext, Notification::class.java).apply {
            putExtra("notification_title", "Reminder")
            putExtra("notification_text", "This is your 1-minute reminder!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + 60 * 1000

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}

