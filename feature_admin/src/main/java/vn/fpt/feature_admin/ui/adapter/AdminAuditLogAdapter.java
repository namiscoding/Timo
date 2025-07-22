package vn.fpt.feature_admin.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.fpt.core.models.AuditLog;
import vn.fpt.core.models.User;
import vn.fpt.feature_admin.R;

public class AdminAuditLogAdapter extends RecyclerView.Adapter<AdminAuditLogAdapter.AuditLogViewHolder> {

    private final List<AuditLog> logs;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, String> cinemaMap;

    public AdminAuditLogAdapter(List<AuditLog> logs, Map<String, String> cinemaMap) {
        this.logs = logs;
        this.cinemaMap = cinemaMap;
    }

    @NonNull
    @Override
    public AuditLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_audit_log, parent, false);
        return new AuditLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuditLogViewHolder holder, int position) {
        AuditLog log = logs.get(position);

        // Set user info
        holder.tvUserName.setText(log.getUserName());
        holder.tvTimestamp.setText(dateFormat.format(log.getTimestamp().toDate()));

        // Set action description
        holder.tvDescription.setText(log.getDescription());

        // Set status
        holder.tvStatus.setText(log.isSuccess() ? "Thành công" : "Thất bại");
        holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(
                log.isSuccess() ? android.R.color.holo_green_light : android.R.color.holo_red_light
        ));

        // Set role
        holder.tvUserRole.setText(log.getUserRole());

        // Set target type
        holder.tvTargetType.setText(log.getTargetType());

        // Set device info
        holder.tvDeviceInfo.setText(log.getDeviceInfo());

        // Set action icon
        setActionIcon(holder.ivActionIcon, log.getAction());

        // Handle expandable details
        holder.itemView.setOnClickListener(v -> {
            boolean isExpanded = holder.expandableDetails.getVisibility() == View.VISIBLE;
            holder.expandableDetails.setVisibility(isExpanded ? View.GONE : View.VISIBLE);

            if (!isExpanded) {
                StringBuilder details = new StringBuilder();
                details.append("ID: ").append(log.getId()).append("\n");
                details.append("User ID: ").append(log.getUserId()).append("\n");
                details.append("Session ID: ").append(log.getSessionId()).append("\n");

                if (log.getTargetId() != null) {
                    details.append("Target ID: ").append(log.getTargetId()).append("\n");
                }

                if (log.getIpAddress() != null) {
                    details.append("IP Address: ").append(log.getIpAddress()).append("\n");
                }

                if (log.getErrorMessage() != null) {
                    details.append("Error: ").append(log.getErrorMessage()).append("\n");
                }

                if (log.getOldData() != null) {
                    details.append("\n--- Dữ liệu trước khi chỉnh sửa ---\n");
                    if ("USER".equals(log.getTargetType()) && log.getOldData() instanceof User) {
                        details.append(getUserJsonWithCinemaName((User) log.getOldData()));
                    } else {
                        details.append(gson.toJson(log.getOldData()));
                    }
                    details.append("\n");
                }

                if (log.getNewData() != null) {
                    details.append("--- Dữ liệu sau khi chỉnh sửa ---\n");
                    if ("USER".equals(log.getTargetType()) && log.getNewData() instanceof User) {
                        details.append(getUserJsonWithCinemaName((User) log.getNewData()));
                    } else {
                        details.append(gson.toJson(log.getNewData()));
                    }
                    details.append("\n");
                }

                holder.tvDetailedInfo.setText(details.toString());
            }
        });
    }

    private String getUserJsonWithCinemaName(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("displayName", user.getDisplayName());
        userMap.put("role", user.getRole());
        userMap.put("active", user.isActive());
        userMap.put("photoUrl", user.getPhotoUrl());
        userMap.put("createdAt", user.getCreatedAt() != null ? dateFormat.format(user.getCreatedAt().toDate()) : null);

        String cinemaId = user.getAssignedCinemaId();
        String cinemaName = cinemaMap.get(cinemaId);
        userMap.put("assignedCinemaName", cinemaName != null ? cinemaName : cinemaId);

        return gson.toJson(userMap);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    private void setActionIcon(ImageView imageView, String action) {
        int iconRes;
        switch (action) {
            case "LOGIN":
                iconRes = R.drawable.ic_login;
                break;
            case "LOGOUT":
                iconRes = R.drawable.ic_logout;
                break;
            case "CREATE":
                iconRes = R.drawable.ic_add;
                break;
            case "UPDATE":
                iconRes = R.drawable.ic_edit;
                break;
            case "DELETE":
                iconRes = R.drawable.ic_delete;
                break;
            case "VIEW":
                iconRes = R.drawable.ic_view;
                break;
            default:
                iconRes = R.drawable.ic_action;
                break;
        }
        imageView.setImageResource(iconRes);
    }

    public void updateData(List<AuditLog> newLogs) {
        logs.clear();
        logs.addAll(newLogs);
        notifyDataSetChanged();
    }

    static class AuditLogViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvTimestamp, tvDescription, tvStatus;
        TextView tvUserRole, tvTargetType, tvDeviceInfo, tvDetailedInfo;
        ImageView ivActionIcon;
        LinearLayout expandableDetails;

        AuditLogViewHolder(View view) {
            super(view);
            tvUserName = view.findViewById(R.id.tvUserName);
            tvTimestamp = view.findViewById(R.id.tvTimestamp);
            tvDescription = view.findViewById(R.id.tvDescription);
            tvStatus = view.findViewById(R.id.tvStatus);
            tvUserRole = view.findViewById(R.id.tvUserRole);
            tvTargetType = view.findViewById(R.id.tvTargetType);
            tvDeviceInfo = view.findViewById(R.id.tvDeviceInfo);
            tvDetailedInfo = view.findViewById(R.id.tvDetailedInfo);
            ivActionIcon = view.findViewById(R.id.ivActionIcon);
            expandableDetails = view.findViewById(R.id.expandableDetails);
        }
    }
}