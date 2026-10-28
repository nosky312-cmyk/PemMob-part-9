package com.example.pertpemmob;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pertpemmob.adapter.PostAdapter;
import com.example.pertpemmob.api.RetrofitClient;
import com.example.pertpemmob.model.Post;
import com.example.pertpemmob.utils.NetworkUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1500;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewPosts;
    private LinearLayout layoutLoading;
    private LinearLayout layoutError;
    private TextView tvErrorMessage;
    private Button btnRetry;

    private PostAdapter postAdapter;
    private int retryCount = 0;
    private final Handler retryHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupListeners();

        // Initial fetch
        fetchPosts(false);
    }

    private void initViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutError = findViewById(R.id.layoutError);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnRetry = findViewById(R.id.btnRetry);

        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.secondary
        );
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(this::onPostClicked);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPosts.setAdapter(postAdapter);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            retryCount = 0;
            fetchPosts(true);
        });

        btnRetry.setOnClickListener(v -> {
            retryCount = 0;
            fetchPosts(false);
        });
    }

    /**
     * Fetches all posts from the API with network check & error handling
     */
    private void fetchPosts(boolean isSwipeRefresh) {
        if (!isSwipeRefresh) {
            showLoadingState();
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showErrorState(getString(R.string.error_no_internet));
            return;
        }

        RetrofitClient.getInstance().getApiService().getPosts().enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                swipeRefreshLayout.setRefreshing(false);
                retryCount = 0;

                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body();
                    if (posts.isEmpty()) {
                        showErrorState(getString(R.string.error_generic));
                    } else {
                        showContentState();
                        postAdapter.setPosts(posts);
                    }
                } else {
                    showErrorState(getString(R.string.error_http, response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    retryHandler.postDelayed(() -> fetchPosts(isSwipeRefresh), RETRY_DELAY_MS * retryCount);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    String message = t.getMessage() != null ? t.getMessage() : getString(R.string.error_generic);
                    showErrorState(getString(R.string.error_generic) + "\n(" + message + ")");
                }
            }
        });
    }

    /**
     * Demonstrates fetching a single post by ID from /posts/{id} when clicked
     */
    private void onPostClicked(Post post) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, getString(R.string.fetching_single_post, post.getId()), Toast.LENGTH_SHORT).show();

        RetrofitClient.getInstance().getApiService().getPostById(post.getId()).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Post detailedPost = response.body();
                    showPostDetailDialog(detailedPost);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.error_http, response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPostDetailDialog(Post post) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.single_post_title, post.getId()))
                .setMessage("User ID: " + post.getUserId() + "\n\n" +
                        "Title:\n" + post.getTitle() + "\n\n" +
                        "Body:\n" + post.getBody())
                .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLoadingState() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        recyclerViewPosts.setVisibility(View.GONE);
    }

    private void showContentState() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        recyclerViewPosts.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showErrorState(String message) {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        recyclerViewPosts.setVisibility(View.GONE);
        tvErrorMessage.setText(message);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        retryHandler.removeCallbacksAndMessages(null);
    }
}
