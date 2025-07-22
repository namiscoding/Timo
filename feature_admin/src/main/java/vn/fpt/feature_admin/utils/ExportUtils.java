package vn.fpt.feature_admin.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.fpt.core.models.AuditLog;

public class ExportUtils {
    private static final String TAG = "ExportUtils";

    public static void exportAuditLogsToCSV(Context context, List<AuditLog> logs) {
        try {
            // Tạo thư mục Downloads nếu chưa có
            File downloadsDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "AuditLogs");
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            // Tạo tên file với timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "audit_logs_" + dateFormat.format(new Date()) + ".csv";
            File file = new File(downloadsDir, fileName);

            // Ghi dữ liệu vào file
            FileWriter writer = new FileWriter(file);

            // Header
            writer.append("Thời gian,Người dùng,Vai trò,Hành động,Loại đối tượng,ID đối tượng,Mô tả,Trạng thái,Thông báo lỗi,IP,Thiết bị,Dữ liệu trước,Dữ liệu sau\n");

            // Dùng Gson để stringify object
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            SimpleDateFormat csvDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            for (AuditLog log : logs) {
                writer.append(csvDateFormat.format(log.getTimestamp().toDate())).append(",");
                writer.append(escapeCSV(log.getUserName())).append(",");
                writer.append(escapeCSV(log.getUserRole())).append(",");
                writer.append(escapeCSV(log.getAction())).append(",");
                writer.append(escapeCSV(log.getTargetType())).append(",");
                writer.append(escapeCSV(log.getTargetId() != null ? log.getTargetId() : "")).append(",");
                writer.append(escapeCSV(log.getDescription())).append(",");
                writer.append(log.isSuccess() ? "Thành công" : "Thất bại").append(",");
                writer.append(escapeCSV(log.getErrorMessage() != null ? log.getErrorMessage() : "")).append(",");
                writer.append(escapeCSV(log.getIpAddress() != null ? log.getIpAddress() : "")).append(",");
                writer.append(escapeCSV(log.getDeviceInfo() != null ? log.getDeviceInfo() : "")).append(",");
                writer.append(escapeCSV(log.getOldData() != null ? gson.toJson(log.getOldData()) : "")).append(",");
                writer.append(escapeCSV(log.getNewData() != null ? gson.toJson(log.getNewData()) : "")).append("\n");
            }

            writer.flush();
            writer.close();

            // Lấy URI và mở file
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "text/csv");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(intent, "Mở file CSV với");
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(chooser);
            }

            DialogUtils.showSuccessDialog(context, "File đã được lưu tại:\n" + file.getAbsolutePath());
            Log.d(TAG, "Export successful: " + file.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Error exporting CSV", e);
            Toast.makeText(context, "Lỗi khi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}
