package com.example.manud_jaya.model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "supabase")
@Data
public class SupabaseConfig {

    private String url;
    private String apiKey;
    private String bucket;

}
