package com.example.Studio21.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve las imágenes guardadas localmente en la URL /uploads/** (alternativa a Cloudinary).
        Path uploadDir = Paths.get("src/main/resources/static/uploads");
        String ubicacion = uploadDir.toAbsolutePath().toUri().toString();
        if (!ubicacion.endsWith("/")) {
            ubicacion = ubicacion + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(ubicacion);
    }
}
