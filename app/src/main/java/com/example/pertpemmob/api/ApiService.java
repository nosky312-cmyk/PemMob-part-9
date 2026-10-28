package com.example.pertpemmob.api;

import com.example.pertpemmob.model.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    /**
     * Endpoint to fetch all posts from /posts
     */
    @GET("posts")
    Call<List<Post>> getPosts();

    /**
     * Endpoint to fetch a single post by ID from /posts/{id}
     */
    @GET("posts/{id}")
    Call<Post> getPostById(@Path("id") int id);
}
