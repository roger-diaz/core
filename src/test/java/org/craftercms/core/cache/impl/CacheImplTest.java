/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.core.cache.impl;

import static org.craftercms.core.cache.CacheItem.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.craftercms.core.cache.CacheItem;
import org.craftercms.core.cache.impl.store.EhCacheStoreAdapter;
import org.craftercms.core.exception.InternalCacheEngineException;
import org.craftercms.core.exception.InvalidScopeException;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * {@link org.craftercms.core.cache.impl.CacheImpl} unit test.
 *
 * @author Alfonso Vásquez
 */
public class CacheImplTest {

    private static final String SCOPE = "test";
    private static final int MAX_ITEMS_IN_MEMORY = 10;

    private static final int ITEM_KEY1 =      1;
    private static final String ITEM_VALUE1 = "Test value #1";
    private static final int ITEM_KEY2 =      2;
    private static final String ITEM_VALUE2 = "Test value #2";
    private static final int ITEM_KEY3 =      3;
    private static final String ITEM_VALUE3 = "Test value #3";
    private static final int ITEM_KEY4 =      4;
    private static final String ITEM_VALUE4 = "Test value #4";
    private static final int ITEM_KEY5 =      5;
    private static final String ITEM_VALUE5 = "Test value #5";

    private static final long EXPIRATION_VALUE1 = 2;

    private static final long REFRESH_FREQUENCY_VALUE1 = 1;
    private static final long REFRESH_FREQUENCY_VALUE2 = 2;

    private static final Object[] DEPENDENCY_KEYS_ITEM3 = { ITEM_KEY1 };
    private static final Object[] DEPENDENCY_KEYS_ITEM4 = { ITEM_KEY2, ITEM_KEY3 };
    private static final Object[] DEPENDENCY_KEYS_ITEM5 = { ITEM_KEY2 };

    private EhCacheStoreAdapter cacheStore;
    private CacheImpl cache;

    @Before
    public void setUp() throws InternalCacheEngineException {
        cacheStore = new EhCacheStoreAdapter();

        cache = new CacheImpl();
        cache.setCacheRefresher(new CacheRefresherImpl());
        cache.setCacheStoreAdapter(cacheStore);

        cache.addScope(SCOPE, MAX_ITEMS_IN_MEMORY);
    }

    @After
    public void tearDown() {
        cacheStore.destroy();
    }

    @Test
    public void testOnTick() throws InternalCacheEngineException, InvalidScopeException {
        cache.put(SCOPE, ITEM_KEY1, ITEM_VALUE1, NEVER_EXPIRE, REFRESH_FREQUENCY_VALUE1, new DummyCacheLoader(),
                ITEM_VALUE1);
        cache.put(SCOPE, ITEM_KEY2, ITEM_VALUE2, NEVER_EXPIRE, REFRESH_FREQUENCY_VALUE2, new DummyCacheLoader(),
                ITEM_VALUE2);
        cache.put(SCOPE, ITEM_KEY3, ITEM_VALUE3, EXPIRATION_VALUE1, NEVER_REFRESH, null);

        // Ticks = 1. Item #1 should be refreshed.
        cache.tick();

        CacheItem item = cache.get(SCOPE, ITEM_KEY1);
        assertEquals(ITEM_VALUE1.toUpperCase(), item.getValue());

        item = cache.get(SCOPE, ITEM_KEY2);
        assertEquals(ITEM_VALUE2, item.getValue());

        item = cache.get(SCOPE, ITEM_KEY3);
        assertEquals(ITEM_VALUE3, item.getValue());

        // Ticks = 2. Items #1 and #2 should be refreshed and #3 removed from cache.
        cache.tick();

        item = cache.get(SCOPE, ITEM_KEY1);
        assertEquals(ITEM_VALUE1.toUpperCase(), item.getValue());

        item = cache.get(SCOPE, ITEM_KEY2);
        assertEquals(ITEM_VALUE2.toUpperCase(), item.getValue());

        item = cache.get(SCOPE, ITEM_KEY3);
        assertNull(item);
    }

    @Test
    public void testDependencyManagement() throws InternalCacheEngineException, InvalidScopeException {
        cache.put(SCOPE, ITEM_KEY1, ITEM_VALUE1);
        cache.put(SCOPE, ITEM_KEY2, ITEM_VALUE2);
        cache.put(SCOPE, ITEM_KEY3, ITEM_VALUE3, Arrays.asList(DEPENDENCY_KEYS_ITEM3));
        cache.put(SCOPE, ITEM_KEY4, ITEM_VALUE4, Arrays.asList(DEPENDENCY_KEYS_ITEM4));
        cache.put(SCOPE, ITEM_KEY5, ITEM_VALUE5, Arrays.asList(DEPENDENCY_KEYS_ITEM5));

        // Items haven't changed, the get should work.
        CacheItem item = cache.getWithDependencyCheck(SCOPE, ITEM_KEY4);
        assertEquals(ITEM_VALUE4, item.getValue());

        // Remove item #2 from cache. If #4 and #5 are requested, they should be removed from cache.
        cache.remove(SCOPE, ITEM_KEY2);

        item = cache.getWithDependencyCheck(SCOPE, ITEM_KEY4);
        assertNull(item);

        item = cache.getWithDependencyCheck(SCOPE, ITEM_KEY5);
        assertNull(item);

        cache.put(SCOPE, ITEM_KEY2, ITEM_VALUE2);
        cache.put(SCOPE, ITEM_KEY4, ITEM_VALUE4, Arrays.asList(DEPENDENCY_KEYS_ITEM4));

        // Since item #4 has just been re-added, a get should work.
        item = cache.getWithDependencyCheck(SCOPE, ITEM_KEY4);
        assertEquals(ITEM_VALUE4, item.getValue());

        // Update item #1. If #3 and #4 are requested, they should be removed from cache.
        cache.put(SCOPE, ITEM_KEY1, ITEM_VALUE1);

        // Request item #4 first so that dependencies are checked recursively.
        item = cache.getWithDependencyCheck(SCOPE, ITEM_KEY4);
        assertNull(item);

        item = cache.getWithDependencyCheck(SCOPE, ITEM_KEY3);
        assertNull(item);
    }

}
