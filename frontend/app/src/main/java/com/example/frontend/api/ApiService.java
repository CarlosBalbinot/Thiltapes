package com.example.frontend.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Body;
import java.util.Map;
import java.util.List;

public interface ApiService {
    @GET("health")
    Call<ApiResponse> healthCheck();

    // ============================================================
    // AUTHENTICATION
    // ============================================================
    @POST("user/login")
    Call<ApiResponse> login(@Body Map<String, Object> data);

    @POST("user/create")
    Call<ApiResponse> register(@Body Map<String, Object> data);

    // ============================================================
    // GAMES
    // ============================================================
    @GET("game/fetch-all")
    Call<List<Map<String, Object>>> getGames();

    @GET("game/find-by-id/{gameId}")
    Call<ApiResponse> getGame(@Path("gameId") String gameId);

    @POST("game/create")
    Call<ApiResponse> createGame(@Body Object request);

    // ============================================================
    // CARDS (Ajustado de "games" para "game" conforme log 404)
    // ============================================================
    @GET("game/{gameId}/cards")
    Call<ApiResponse> getGameCards(@Path("gameId") String gameId);

    @GET("game/{gameId}/nearby")
    Call<ApiResponse> getNearbyCards(
        @Path("gameId") String gameId,
        @Query("lat") double lat,
        @Query("lng") double lng
    );

    // ============================================================
    // INVENTORY
    // ============================================================
    @GET("user/get-inventory/{player_id}")
    Call<ApiResponse> getMyInventory(@Query("playerId") String playerId);
}
