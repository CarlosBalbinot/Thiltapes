package com.example.frontend.api;

import com.example.frontend.BuildConfig;

/**
 * Configurações da API
 * Centraliza URLs e configurações da aplicação
 * Permite trocar facilmente entre dev e produção
 */
public class AppConfig {
    /**
     * URL base da API
     * Dev: Emulador Android conecta ao PC via 10.0.2.2:3000
     * Prod: URL https final (seu time atualizar depois)
     */
    public static final String API_BASE_URL = BuildConfig.DEBUG
        ? "http://10.0.2.2:3000/api/"        // Desenvolvimento (emulador)
        : "https://api.thiltapes.com/api/";  // Produção (seu time atualizar)

    /**
     * Timeout das requisições em segundos
     */
    public static final int API_TIMEOUT_SECONDS = 30;

    /**
     * Header de versão da API
     */
    public static final String API_VERSION = "1.0.0";

    /**
     * User-Agent identificador da app
     */
    public static final String USER_AGENT = "Thiltapes-Android/" + API_VERSION;
}
