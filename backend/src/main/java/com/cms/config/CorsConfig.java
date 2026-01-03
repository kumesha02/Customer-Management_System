package com.cms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 🎓 LESSON: CORS - Cross-Origin Resource Sharing
 * ============================================================================
 * 
 * WHAT IS CORS?
 * --------------
 * CORS is a security feature implemented by web browsers.
 * 
 * THE PROBLEM (Same-Origin Policy):
 * -----------------------------------
 * By default, browsers block requests from one origin to another:
 * 
 * Scenario:
 * - Frontend runs on: http://localhost:3000 (React)
 * - Backend runs on: http://localhost:8080 (Spring Boot)
 * 
 * When React tries to call Spring Boot API:
 * 
 * fetch('http://localhost:8080/api/customers')
 * 
 * Browser says: ❌ "Blocked by CORS policy: No 'Access-Control-Allow-Origin'
 * header"
 * 
 * WHY? Different origins (different ports = different origins):
 * - Origin 1: http://localhost:3000
 * - Origin 2: http://localhost:8080
 * 
 * This is a SECURITY FEATURE to prevent malicious sites from accessing your
 * API!
 * 
 * THE SOLUTION:
 * --------------
 * Backend must send CORS headers telling browser: "It's OK to accept requests
 * from localhost:3000"
 * 
 * Headers:
 * - Access-Control-Allow-Origin: http://localhost:3000
 * - Access-Control-Allow-Methods: GET, POST, PUT, DELETE
 * - Access-Control-Allow-Headers: Content-Type, Authorization
 * - Access-Control-Allow-Credentials: true (for cookies/auth)
 * 
 * ============================================================================
 * HOW CORS WORKS (Preflight Request):
 * ============================================================================
 * 
 * For "complex" requests (POST, PUT, DELETE, custom headers), browser sends:
 * 
 * 1. PREFLIGHT REQUEST (OPTIONS):
 * OPTIONS /api/customers
 * Origin: http://localhost:3000
 * Access-Control-Request-Method: POST
 * Access-Control-Request-Headers: content-type
 * 
 * 2. SERVER RESPONSE:
 * Access-Control-Allow-Origin: http://localhost:3000
 * Access-Control-Allow-Methods: POST, GET, PUT, DELETE
 * Access-Control-Allow-Headers: content-type
 * Access-Control-Max-Age: 3600
 * 
 * 3. ACTUAL REQUEST:
 * POST /api/customers
 * (Only sent if preflight succeeds)
 * 
 * ============================================================================
 * 
 * @Configuration:
 * 
 *                 - Marks this as a configuration class
 *                 - Spring scans for @Bean methods
 *                 - Methods returning beans are added to Spring context
 * 
 *                 ============================================================================
 */
@Configuration
public class CorsConfig {

    /**
     * Configure CORS globally for all endpoints
     * 
     * @return WebMvcConfigurer bean
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                /**
                 * 🎓 CORS CONFIGURATION EXPLAINED:
                 * 
                 * addMapping("/**"):
                 * - Apply CORS to ALL endpoints
                 * - /** means all paths (/api/customers, /api/upload, etc.)
                 * 
                 * allowedOrigins("http://localhost:3000"):
                 * - Allow requests from React dev server
                 * - In production, change to your actual domain
                 * - Can specify multiple: .allowedOrigins("https://example.com",
                 * "https://app.example.com")
                 * 
                 * allowedMethods("*"):
                 * - Allow all HTTP methods (GET, POST, PUT, DELETE, PATCH, OPTIONS)
                 * - Can restrict: .allowedMethods("GET", "POST")
                 * 
                 * allowedHeaders("*"):
                 * - Allow all headers
                 * - Common headers: Content-Type, Authorization, X-Requested-With
                 * 
                 * allowCredentials(true):
                 * - Allow cookies and authentication headers
                 * - Required for session-based auth or JWT in cookies
                 * - IMPORTANT: When true, allowedOrigins cannot be "*"
                 * 
                 * maxAge(3600):
                 * - Cache preflight response for 1 hour (3600 seconds)
                 * - Reduces unnecessary preflight requests
                 * - Browser won't send preflight again for 1 hour
                 */
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:3000", // React dev server
                                "http://localhost:3001" // Alternative port
                )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

    /**
     * 🎓 PRODUCTION CONSIDERATIONS:
     * ============================================================================
     * 
     * 1. ENVIRONMENT-SPECIFIC CONFIGURATION:
     * Use application.properties for different environments:
     * 
     * cors.allowed.origins=http://localhost:3000
     * 
     * Then inject with @Value:
     * @Value("${cors.allowed.origins}")
     * private String allowedOrigins;
     * 
     * registry.addMapping("/**").allowedOrigins(allowedOrigins.split(","))
     * 
     * 2. SECURITY:
     * - NEVER use allowedOrigins("*") with allowCredentials(true)
     * - Be specific about allowed origins
     * - Consider using a whitelist
     * 
     * 3. CDN/MULTIPLE DOMAINS:
     * If serving frontend from multiple domains:
     * .allowedOrigins("https://app.example.com", "https://www.example.com")
     * 
     * ============================================================================
     */
}
