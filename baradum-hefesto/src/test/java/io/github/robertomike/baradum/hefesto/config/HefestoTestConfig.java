package io.github.robertomike.baradum.hefesto.config;

import jakarta.persistence.EntityManager;

/**
 * Singleton to manage global Hefesto EntityManager for tests.
 * Hefesto queries use the globally configured EntityManager.
 */
public class HefestoTestConfig {
    private static EntityManager globalEntityManager;
    
    public static void setEntityManager(EntityManager em) {
        globalEntityManager = em;
    }
    
    public static EntityManager getEntityManager() {
        if (globalEntityManager == null) {
            throw new IllegalStateException("EntityManager not configured. Call HefestoTestConfig.setEntityManager() first.");
        }
        return globalEntityManager;
    }
    
    public static void clear() {
        globalEntityManager = null;
    }
}
