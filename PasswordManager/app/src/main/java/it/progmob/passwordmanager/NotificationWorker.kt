import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import it.progmob.passwordmanager.Notification

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        // Perform the background task here
        val itemName = inputData.getString("itemName") ?: return Result.failure()
        val userEmail = inputData.getString("userEmail") ?: return Result.failure()
        val id = inputData.getInt("notificationID", -1)
        val triggerTime = inputData.getLong("triggerTime", -1)
        if (triggerTime.toInt() == -1 || id == -1) return Result.failure()

        performBackgroundTask(itemName, userEmail, id, triggerTime)
        return Result.success()
    }
    @SuppressLint("ScheduleExactAlarm")
    private fun performBackgroundTask(itemName: String, userEmail: String, id: Int, triggerTime: Long) {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        val intent = Intent(applicationContext, Notification::class.java).apply {
            putExtra("notification_title", "$itemName will expire tomorrow!")
            putExtra("notification_text", "$itemName from $userEmail item's will expire tomorrow, an update is required.")
            putExtra("notification_id", id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}

