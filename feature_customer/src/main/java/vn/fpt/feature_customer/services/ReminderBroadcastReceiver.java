package vn.fpt.feature_customer.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import vn.fpt.feature_customer.R; // Đảm bảo bạn có R từ module của mình

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_CHANNEL_ID = "movie_reminder_channel";
    public static final String EXTRA_FILM_TITLE = "extra_film_title";
    public static final String EXTRA_SHOW_TIME = "extra_show_time";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Lấy thông tin phim từ Intent
        String filmTitle = intent.getStringExtra(EXTRA_FILM_TITLE);
        String showTime = intent.getStringExtra(EXTRA_SHOW_TIME);
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);

        // Tạo nội dung thông báo
        String title = "🔔 Sắp đến giờ xem phim!";
        String body = "Phim \"" + filmTitle + "\" sẽ bắt đầu lúc " + showTime + ". Chúc bạn xem phim vui vẻ!";

        // Hiển thị thông báo
        showNotification(context, title, body, notificationId);
    }

    private void showNotification(Context context, String title, String body, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Từ Android Oreo (API 26) trở lên, cần có Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Nhắc nhở xem phim",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Kênh thông báo cho lịch chiếu phim sắp tới");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.oval_btn_background)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}