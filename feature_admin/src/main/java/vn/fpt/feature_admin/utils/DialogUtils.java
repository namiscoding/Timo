package vn.fpt.feature_admin.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import vn.fpt.feature_admin.R;

public class DialogUtils {

    public static void showConfirmDeleteDialog(Context context, String message, Runnable onConfirm) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        ((TextView) view.findViewById(R.id.message)).setText(message);

        view.findViewById(R.id.btnOk).setOnClickListener(v -> {
            dialog.dismiss();
            onConfirm.run();
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public static void showSuccessDialog(Context context, String message) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .create();

        ((ImageView) view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_success);
        ((TextView) view.findViewById(R.id.title)).setText("Thành công");
        ((TextView) view.findViewById(R.id.title)).setTextColor(ContextCompat.getColor(context, R.color.success_green));
        ((TextView) view.findViewById(R.id.message)).setText(message);

        Button btnOk = view.findViewById(R.id.btnOk);
        btnOk.setText("OK");
        btnOk.setOnClickListener(v -> dialog.dismiss());

        // Ẩn nút Cancel trong dialog thành công
        view.findViewById(R.id.btnCancel).setVisibility(View.GONE);

        dialog.show();
    }
}
