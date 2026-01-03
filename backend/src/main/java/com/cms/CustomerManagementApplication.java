package com.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 🎓 LESSON: Spring Boot Main Application Class
 * ============================================================================
 * 
 * This is the entry point of our Spring Boot application. Let's understand
 * what's happening here:
 * 
 * @SpringBootApplication is a convenience annotation that combines three
 *                        others:
 * 
 *                        1. @Configuration:
 *                        - Marks this class as a source of bean definitions
 *                        - Beans are objects managed by Spring's IoC (Inversion
 *                        of Control) container
 * 
 *                        2. @EnableAutoConfiguration:
 *                        - Tells Spring Boot to automatically configure your
 *                        application based on
 *                        the dependencies in your classpath
 *                        - For example, it sees MariaDB driver → configures
 *                        DataSource
 *                        - Sees Spring Data JPA → configures
 *                        EntityManagerFactory, TransactionManager
 *                        - This is the "magic" that makes Spring Boot so
 *                        productive!
 * 
 *                        3. @ComponentScan:
 *                        - Tells Spring to scan this package and all
 *                        sub-packages for components
 *                        - Components
 *                        include @Controller, @Service, @Repository, @Component
 *                        - Spring will automatically create instances of these
 *                        classes and manage them
 * 
 *                        ============================================================================
 *                        CONCEPT: Inversion of Control (IoC) & Dependency
 *                        Injection (DI)
 *                        ============================================================================
 * 
 *                        Traditional approach:
 *                        - Your code creates objects using "new"
 *                        - You manage object lifecycles
 *                        - Tight coupling between classes
 * 
 *                        Spring approach:
 *                        - Spring creates and manages objects (called Beans)
 *                        - You declare dependencies using @Autowired
 *                        - Spring "injects" the dependencies for you
 *                        - Loose coupling, easier testing, better design
 * 
 *                        Example:
 *                        Instead of:
 *                        CustomerService service = new CustomerService(new
 *                        CustomerRepository());
 * 
 *                        With Spring:
 * @Autowired
 *            private CustomerService service; // Spring injects this
 *            automatically!
 * 
 *            ============================================================================
 */
@SpringBootApplication
public class CustomerManagementApplication {

    /**
     * Main method - application entry point
     * 
     * SpringApplication.run() does several things:
     * 1. Creates ApplicationContext (Spring's IoC container)
     * 2. Scans for components
     * 3. Configures beans
     * 4. Starts embedded Tomcat server
     * 5. Deploy your application
     * 
     * All of this in ONE line of code! 🎉
     */
    public static void main(String[] args) {
        SpringApplication.run(CustomerManagementApplication.class, args);

        System.out.println("\n" +
                "╔════════════════════════════════════════════════════════════╗\n" +
                "║  Customer Management System Started Successfully! 🚀      ║\n" +
                "║                                                            ║\n" +
                "║  Application running at: http://localhost:8080            ║\n" +
                "║  API Base Path: http://localhost:8080/api                 ║\n" +
                "║                                                            ║\n" +
                "║  Ready to accept requests!                                ║\n" +
                "╚════════════════════════════════════════════════════════════╝\n");
    }
}
