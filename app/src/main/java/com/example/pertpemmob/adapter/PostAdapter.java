package com.example.pertpemmob.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pertpemmob.R;
import com.example.pertpemmob.model.Post;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> postList = new ArrayList<>();
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public PostAdapter() {
    }

    public PostAdapter(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setOnPostClickListener(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.postList.clear();
        if (posts != null) {
            this.postList.addAll(posts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post, listener);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvPostId;
        private final TextView tvUserId;
        private final TextView tvTitle;
        private final TextView tvBody;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPostId = itemView.findViewById(R.id.tvPostId);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvBody = itemView.findViewById(R.id.tvBody);
        }

        public void bind(final Post post, final OnPostClickListener listener) {
            tvPostId.setText(itemView.getContext().getString(R.string.post_id_format, post.getId()));
            tvUserId.setText(itemView.getContext().getString(R.string.user_id_format, post.getUserId()));
            tvTitle.setText(post.getTitle());
            tvBody.setText(post.getBody());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });
        }
    }
}
