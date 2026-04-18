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
     * Configurada por build type no Gradle
     */
    public static final String API_BASE_URL = BuildConfig.API_BASE_URL;

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
