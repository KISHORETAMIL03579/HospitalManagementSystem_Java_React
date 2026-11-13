package com.careflow.hospitalmanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.ResourceTransformerChain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Primary
@Component
public class CareFlowSwaggerIndexTransformer extends SwaggerIndexPageTransformer {

    public CareFlowSwaggerIndexTransformer(
            SwaggerUiConfigProperties swaggerUiConfig,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerUiConfigParameters swaggerUiConfigParameters,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider) {
        super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerUiConfigParameters, swaggerWelcomeCommon, objectMapperProvider);
    }

    @Override
    public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformerChain) throws IOException {
        Resource transformed = super.transform(request, resource, transformerChain);

        // ONLY transform the Swagger UI HTML page, NEVER alter JavaScript or CSS webjar bundles!
        String filename = resource.getFilename();
        if (filename == null || !filename.endsWith(".html")) {
            return transformed;
        }

        try (InputStream is = transformed.getInputStream()) {
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String customHead = """
                <link rel="stylesheet" type="text/css" href="/custom-swagger/careflow.css" />
                <link rel="stylesheet" type="text/css" href="/custom-swagger/custom-auth.css" />
                """;
            String customBody = """
                <script src="/custom-swagger/custom-auth.js" charset="UTF-8"></script>
                """;
            if (html.contains("</head>")) {
                html = html.replace("</head>", customHead + "\n</head>");
            }
            if (html.contains("</body>")) {
                html = html.replace("</body>", customBody + "\n</body>");
            }
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return transformed.getFilename();
                }

                @Override
                public long lastModified() throws IOException {
                    return transformed.lastModified();
                }
            };
        }
    }
}
