package com.example.data.remote

import com.example.data.model.Category
import com.example.data.model.VideoItem
import com.example.data.model.WatchProgress
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit contract for communicating with the external Node.js backend via HTTPS.
 * The Node.js server is responsible for interacting with Supabase (PostgreSQL, Storage, Auth),
 * keeping all database passwords and Supabase service-role keys secure on the server side.
 *
 * NOTE: These endpoints are placeholders designed to match standard REST conventions
 * expected from the user's Node.js Express/Fastify service.
 */
interface WatchFlixApiService {

  // PLACEHOLDER ENDPOINT: Fetches full movie/series catalog from Node.js server
  @GET("api/v1/videos")
  suspend fun getVideos(
    @Query("category") category: String? = null
  ): Response<List<VideoItem>>

  // PLACEHOLDER ENDPOINT: Fetches single video details
  @GET("api/v1/videos/{id}")
  suspend fun getVideoDetails(
    @Path("id") videoId: String
  ): Response<VideoItem>

  // PLACEHOLDER ENDPOINT: Fetches available categories
  @GET("api/v1/categories")
  suspend fun getCategories(): Response<List<Category>>

  // PLACEHOLDER ENDPOINT: Syncs watch playback progress to Supabase via Node.js
  @POST("api/v1/progress")
  suspend fun updateWatchProgress(
    @Body progress: WatchProgress
  ): Response<Unit>

  // PLACEHOLDER ENDPOINT: Health check for Node.js server connectivity
  @GET("api/v1/health")
  suspend fun checkHealth(): Response<Map<String, String>>
}
