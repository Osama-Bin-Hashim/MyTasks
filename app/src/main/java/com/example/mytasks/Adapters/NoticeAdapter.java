package com.example.mytasks.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytasks.AppDatabase;
import com.example.mytasks.Notice;
import com.example.mytasks.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private List<Notice> noticeList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
    private boolean isManager;

    public NoticeAdapter(boolean isManager) {
        this.isManager = isManager;
    }

    public void setNoticeList(List<Notice> noticeList) {
        this.noticeList = noticeList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);
        holder.bind(notice, dateFormat, isManager, position, this);
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTimestamp, tvContent;
        ImageButton btnDeleteNotice;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoticeItemTitle);
            tvTimestamp = itemView.findViewById(R.id.tvNoticeItemTimestamp);
            tvContent = itemView.findViewById(R.id.tvNoticeItemContent);
            btnDeleteNotice = itemView.findViewById(R.id.btnDeleteNotice);
        }

        public void bind(Notice notice, SimpleDateFormat dateFormat, boolean isManager, int position, NoticeAdapter adapter) {
            Context context = itemView.getContext();
            tvTitle.setText(notice.title);
            tvContent.setText(notice.content);
            tvTimestamp.setText(dateFormat.format(new Date(notice.timestamp)));

            if (isManager) {
                btnDeleteNotice.setVisibility(View.VISIBLE);
                btnDeleteNotice.setOnClickListener(v -> {
                    // CLICK-TIME VERIFICATION GATE
                    SharedPreferences pref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    int currentSessionUserId = pref.getInt("LOGGED_IN_USER_ID", -1);

                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        // Double-check project ownership if needed, but here we trust isManager was passed correctly
                        // and re-verify session exists.
                        if (currentSessionUserId != -1) {
                            AppDatabase db = AppDatabase.getInstance(context);
                            db.noticeDao().deleteNotice(notice);

                            ((android.app.Activity) context).runOnUiThread(() -> {
                                adapter.noticeList.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, adapter.noticeList.size());
                                Toast.makeText(context, "Notice deleted successfully", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            ((android.app.Activity) context).runOnUiThread(() -> 
                                Toast.makeText(context, "Security Alert: Unauthorized Deletion!", Toast.LENGTH_SHORT).show());
                        }
                    });
                });
            } else {
                btnDeleteNotice.setVisibility(View.GONE);
            }
        }
    }
}
