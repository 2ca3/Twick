package com.appspot.twick;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * @author 2ca3
 *
 */
public final class PMF {
    private static final PersistenceManagerFactory pmfInstance =
        JDOHelper.getPersistenceManagerFactory("transactions-optional");

    private PMF() {}

    /**
     * @return pmfInstance
     */
    public static PersistenceManagerFactory get() {
        return pmfInstance;
    }
}

