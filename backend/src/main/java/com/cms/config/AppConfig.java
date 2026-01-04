package com.cms.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 🎓 LESSON: Application Configuration - Creating Beans
 * ============================================================================
 * 
 * WHAT IS A BEAN?
 * ----------------
 * A Bean is an object managed by Spring's IoC (Inversion of Control) container.
 * 
 * Instead of YOU creating objects:
 * ModelMapper mapper = new ModelMapper(); // Manual creation
 * 
 * Spring creates and manages them:
 * 
 * @Autowired
 *            private ModelMapper mapper; // Spring injects this
 * 
 *            BENEFITS:
 *            ---------
 *            1. SINGLE INSTANCE (Singleton by default):
 *            - One ModelMapper for entire application
 *            - Saves memory and initialization time
 * 
 *            2. DEPENDENCY INJECTION:
 *            - Any class can autowire ModelMapper
 *            - No need to pass it around manually
 * 
 *            3. LIFECYCLE MANAGEMENT:
 *            - Spring creates beans on startup
 *            - Destroys them on shutdown
 *            - Handles initialization and cleanup
 * 
 *            4. EASY TESTING:
 *            - Can replace with mock implementations
 *            - No need to modify code
 * 
 *            ============================================================================
 * @Bean ANNOTATION:
 *       ============================================================================
 * 
 *       Methods annotated with @Bean produce objects managed by Spring.
 * 
 *       Spring calls this method ONCE on startup and stores the result.
 *       Whenever ModelMapper is needed, Spring injects that instance.
 * 
 *       ============================================================================
 */
@Configuration
public class AppConfig {

    /**
     * Create ModelMapper bean for DTO conversions
     * 
     * @return ModelMapper instance
     */
    @Bean
    public ModelMapper modelMapper() {
        /**
         * 🎓 ModelMapper: Object-to-Object Mapping
         * ==========================================
         * 
         * ModelMapper automatically maps fields between objects.
         * 
         * Usage in services:
         * 
         * @Autowired
         *            private ModelMapper modelMapper;
         * 
         *            CustomerDTO dto = modelMapper.map(customer, CustomerDTO.class);
         * 
         *            HOW IT WORKS:
         *            - Uses reflection to find matching field names
         *            - Copies values from source to destination
         *            - Handles nested objects
         *            - Supports custom mappings for complex scenarios
         * 
         *            EXAMPLE:
         * 
         *            Customer (Entity):
         *            - id: 1
         *            - name: "John"
         *            - dateOfBirth: 1990-01-01
         * 
         *            modelMapper.map(customer, CustomerDTO.class)
         * 
         *            CustomerDTO:
         *            - id: 1
         *            - name: "John"
         *            - dateOfBirth: 1990-01-01
         * 
         *            All fields copied automatically! 🎉
         * 
         *            WHY USE MODELMAPPER?
         *            --------------------
         *            Without it:
         *            CustomerDTO dto = new CustomerDTO();
         *            dto.setId(entity.getId());
         *            dto.setName(entity.getName());
         *            dto.setDateOfBirth(entity.getDateOfBirth());
         *            dto.setNicNumber(entity.getNicNumber());
         *            // ... 20+ lines for complex objects
         * 
         *            With ModelMapper:
         *            CustomerDTO dto = modelMapper.map(entity, CustomerDTO.class);
         *            // Done! 1 line!
         * 
         *            LIMITATIONS:
         *            ------------
         *            - Doesn't work well with complex nested structures
         *            - Sometimes requires custom configuration
         *            - Can be slow for large datasets (reflection overhead)
         * 
         *            For simple DTOs, manual mapping might be clearer.
         *            For complex DTOs, ModelMapper saves time.
         * 
         *            In our project, we use MIXED APPROACH:
         *            - ModelMapper for simple conversions
         *            - Manual mapping for complex nested objects (Customer →
         *            CustomerDTO)
         */
        ModelMapper modelMapper = new ModelMapper();

        /**
         * 🎓 MODELMAPPER CONFIGURATION:
         * 
         * These settings control how ModelMapper behaves:
         */

        // STRICT matching: Field names must match exactly
        // Prevents accidental mappings between unrelated fields
        // modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // SKIP NULL values: Don't overwrite destination with null values
        // Useful for partial updates
        // modelMapper.getConfiguration().setSkipNullEnabled(true);

        // For learning, we'll use default settings (they work well!)

        return modelMapper;
    }

    /**
     * 🎓 OTHER COMMON BEANS:
     * ============================================================================
     * 
     * In real applications, you might define beans for:
     * 
     * 1. REST TEMPLATE (HTTP client):
     * 
     * @Bean
     *       public RestTemplate restTemplate() {
     *       return new RestTemplate();
     *       }
     * 
     *       2. PASSWORD ENCODER (security):
     * @Bean
     *       public PasswordEncoder passwordEncoder() {
     *       return new BCryptPasswordEncoder();
     *       }
     * 
     *       3. OBJECT MAPPER (JSON):
     * @Bean
     *       public ObjectMapper objectMapper() {
     *       ObjectMapper mapper = new ObjectMapper();
     *       mapper.registerModule(new JavaTimeModule());
     *       return mapper;
     *       }
     * 
     *       4. ASYNC EXECUTOR (background tasks):
     * @Bean
     *       public Executor taskExecutor() {
     *       ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
     *       executor.setCorePoolSize(5);
     *       executor.setMaxPoolSize(10);
     *       return executor;
     *       }
     * 
     *       ============================================================================
     */
}
