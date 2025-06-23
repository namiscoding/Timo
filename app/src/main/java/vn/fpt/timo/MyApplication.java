package vn.fpt.timo;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Firebase khởi tạo một lần cho toàn ứng dụng
        FirebaseApp.initializeApp(this);
    }
}
