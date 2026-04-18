package com.example.frontend.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.Query;
import java.util.Map;

/**
 * Interface Retrofit - Define todos os endpoints da API
 * 
 * Padrão:
 * - Métodos HTTP: GET, POST, PUT, DELETE
 * - Path params: @Path("param")
 * - Query params: @Query("param")
 * - Body: @Body
 * 
 * Seu time adiciona novos métodos conforme novos endpoints são criados
 * 
 * Exemplo para seu time adicionar depois:
 * ╔════════════════════════════════════════════════════════════════╗
 * ║ // GAMES                                                       ║
 * ║ @GET("games")                                                 ║
 * ║ Call<ApiResponse> getGames();                                 ║
 * ║                                                                ║
 * ║ @GET("games/{gameId}")                                        ║
 * ║ Call<ApiResponse> getGame(@Path("gameId") String gameId);     ║
 * ║                                                                ║
 * ║ @POST("games")                                                ║
 * ║ Call<ApiResponse> createGame(@Body CreateGameRequest request);║
 * ║                                                                ║
 * ║ // INVENTORY                                                   ║
 * ║ @POST("games/{gameId}/collect")                               ║
 * ║ Call<ApiResponse> collectCard(                                ║
 * ║   @Path("gameId") String gameId,                              ║
 * ║   @Body CollectCardRequest request                            ║
 * ║ );                                                             ║
 * ╚════════════════════════════════════════════════════════════════╝
 */
public interface ApiService {
    /**
     * GET /api/health
     * Verifica se a API está online
     * Sem autenticação
     */
    @GET("health")
    Call<ApiResponse> healthCheck();

    // ============================================================
    // AUTHENTICATION
    // ============================================================
    @POST("auth/login")
    Call<ApiResponse> login(@Body Map<String, Object> data);

    @POST("auth/register")
    Call<ApiResponse> register(@Body Map<String, Object> data);

    // ============================================================
    // GAMES
    // ============================================================
    @GET("games")
    Call<ApiResponse> getGames();

    @GET("games/{gameId}")
    Call<ApiResponse> getGame(@Path("gameId") String gameId);

    @POST("games")
    Call<ApiResponse> createGame(@Body Object request);

    // ============================================================
    // CARDS
    // ============================================================
    @GET("games/{gameId}/cards")
    Call<ApiResponse> getGameCards(@Path("gameId") String gameId);

    @GET("games/{gameId}/nearby")
    Call<ApiResponse> getNearbyCards(
        @Path("gameId") String gameId,
        @Query("lat") double lat,
        @Query("lng") double lng
    );

    @POST("games/{gameId}/cards")
    Call<ApiResponse> createCard(@Path("gameId") String gameId, @Body Object request);

    // ============================================================
    // INVENTORY
    // ============================================================
    @GET("players/me/inventory")
    Call<ApiResponse> getMyInventory();

    @POST("games/{gameId}/collect")
    Call<ApiResponse> collectCard(@Path("gameId") String gameId, @Body Object request);
}
