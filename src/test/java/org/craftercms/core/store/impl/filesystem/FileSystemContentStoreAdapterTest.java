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
package org.craftercms.core.store.impl.filesystem;

import org.apache.commons.collections.Predicate;
import org.dom4j.Element;
import org.dom4j.Node;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.util.CollectionUtils;
import org.craftercms.core.util.cache.CacheCallback;
import org.craftercms.core.util.cache.CacheTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.craftercms.core.service.CachingOptions.DEFAULT_CACHING_OPTIONS;
import static org.craftercms.core.service.Context.DEFAULT_CACHE_ON;
import static org.craftercms.core.service.Context.DEFAULT_MAX_ALLOWED_ITEMS_IN_CACHE;

/**
 * @author Alfonso Vásquez
 */
public class FileSystemContentStoreAdapterTest {

    private static final String DESCRIPTOR_FILE_EXTENSION = ".xml";
    private static final String METADATA_FILE_EXTENSION = ".meta.xml";

    private static final String CLASSPATH_STORE_ROOT_FOLDER_PATH = "stores/" + FileSystemContentStoreAdapterTest.class.getSimpleName();

    private static final String FOLDER_NAME = "folder";
    private static final String FOLDER_PATH = "/" + FOLDER_NAME;
    private static final String FOLDER_METADATA_FILE_PATH = FOLDER_PATH + METADATA_FILE_EXTENSION;

    private static final String DESCRIPTOR_NAME = "descriptor.xml";
    private static final String DESCRIPTOR_PATH = FOLDER_PATH + "/" + DESCRIPTOR_NAME;

    private static final String CRAFTER_CMS_LOGIC_NAME = "craftercms_logo.png";
    private static final String CRAFTER_CMS_LOGO_PATH = FOLDER_PATH + "/" + CRAFTER_CMS_LOGIC_NAME;
    private static final String CRAFTER_CMS_LOGO_METADATA_FILE_PATH = FOLDER_PATH + "/craftercms_logo" + METADATA_FILE_EXTENSION;

    private static final String HIDDEN_FILE_NAME = ".hidden";

    private FileSystemContentStoreAdapter storeAdapter;
    private CacheTemplate cacheTemplate;

    @Before
    public void setUp() throws Exception {
        setUpTestCacheTemplate();
        setUpTestStoreAdapter();
    }

    @Test
    public void testGetFolderItem() throws Exception {
        Context context = createTestContext(true);

        Item item = storeAdapter.getItem(context, DEFAULT_CACHING_OPTIONS, FOLDER_PATH, true);
        assertNotNull(item);
        assertEquals(FOLDER_NAME, item.getName());
        assertEquals(FOLDER_PATH, item.getUrl());
        assertTrue(item.isFolder());
        assertEquals(FOLDER_METADATA_FILE_PATH, item.getDescriptorUrl());
        assertNotNull(item.getDescriptorDom());

        Element permission = (Element) item.getDescriptorDom().selectSingleNode("/folder-metadata/permissions/permission");
        assertNotNull(permission);

        Element user = permission.element("user");
        assertNotNull(user);
        assertEquals("admin", user.getText());

        Element allowedActions = permission.element("allowed-actions");
        assertNotNull(allowedActions);
        assertEquals("read-only", allowedActions.getText());
    }

    @Test
    public void testGetDescriptorItem() throws Exception {
        Context context = createTestContext(true);

        Item item = storeAdapter.getItem(context, DEFAULT_CACHING_OPTIONS, DESCRIPTOR_PATH, true);
        assertDescriptorItem(item);
    }

    @Test
    public void testGetStaticAssetItem() throws Exception {
        Context context = createTestContext(true);

        Item item = storeAdapter.getItem(context, DEFAULT_CACHING_OPTIONS, CRAFTER_CMS_LOGO_PATH, true);
        assertCrafterCMSLogoItem(item);
    }

    @Test
    public void testGetItems() throws Exception {
        Context context = createTestContext(true);

        List<Item> items = storeAdapter.getItems(context, DEFAULT_CACHING_OPTIONS, FOLDER_PATH, true);
        assertNotNull(items);

        Collections.sort(items, ItemComparator.INSTANCE);

        assertEquals(2, items.size());
        assertCrafterCMSLogoItem(items.get(0));
        assertDescriptorItem(items.get(1));
    }

    @Test
    public void testIgnoreHidden() throws Exception {
        Context context = createTestContext(true);

        List<Item> items = storeAdapter.getItems(context, DEFAULT_CACHING_OPTIONS, FOLDER_PATH, false);
        assertNotNull(items);
        assertEquals(2, items.size());

        context = createTestContext(false);

        items = storeAdapter.getItems(context, DEFAULT_CACHING_OPTIONS, FOLDER_PATH, false);

        assertNotNull(items);
        assertEquals(3, items.size());
        assertTrue(CollectionUtils.exists(items, new Predicate() {

            @Override
            public boolean evaluate(Object object) {
                return HIDDEN_FILE_NAME.equals(((Item) object).getName());
            }

        }));
    }

    private Context createTestContext(boolean ignoreHiddenFiles) {
        return storeAdapter.createContext("0", null, null, null, CLASSPATH_STORE_ROOT_FOLDER_PATH, DEFAULT_CACHE_ON,
                DEFAULT_MAX_ALLOWED_ITEMS_IN_CACHE, ignoreHiddenFiles);
    }

    private void setUpTestCacheTemplate() {
        cacheTemplate = mock(CacheTemplate.class);
        when(cacheTemplate.execute(any(Context.class), eq(DEFAULT_CACHING_OPTIONS), any(CacheCallback.class), anyVararg())).thenAnswer(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return ((CacheCallback<?>) invocation.getArguments()[2]).doCacheable();
                    }
                });
    }

    private void setUpTestStoreAdapter() throws IOException {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResource(CLASSPATH_STORE_ROOT_FOLDER_PATH)).thenReturn(new ClassPathResource(
                CLASSPATH_STORE_ROOT_FOLDER_PATH));

        storeAdapter = new FileSystemContentStoreAdapter();
        storeAdapter.setCacheTemplate(cacheTemplate);
        storeAdapter.setResourceLoader(resourceLoader);
        storeAdapter.setDescriptorFileExtension(DESCRIPTOR_FILE_EXTENSION);
        storeAdapter.setMetadataFileExtension(METADATA_FILE_EXTENSION);
    }

    private void assertCrafterCMSLogoItem(Item item) {
        assertNotNull(item);
        assertEquals(CRAFTER_CMS_LOGIC_NAME, item.getName());
        assertEquals(CRAFTER_CMS_LOGO_PATH, item.getUrl());
        assertFalse(item.isFolder());
        assertEquals(CRAFTER_CMS_LOGO_METADATA_FILE_PATH, item.getDescriptorUrl());
        assertNotNull(item.getDescriptorDom());

        Element resolution = (Element) item.getDescriptorDom().selectSingleNode("/image-metadata/resolution");
        assertNotNull(resolution);

        Element width = resolution.element("width");
        assertNotNull(width);
        assertEquals(346, Integer.parseInt(width.getText()));

        Element height = resolution.element("height");
        assertNotNull(height);
        assertEquals(96, Integer.parseInt(height.getText()));
    }

    private void assertDescriptorItem(Item item) {
        assertNotNull(item);
        assertEquals(DESCRIPTOR_NAME, item.getName());
        assertEquals(DESCRIPTOR_PATH, item.getUrl());
        assertFalse(item.isFolder());
        assertEquals(DESCRIPTOR_PATH, item.getDescriptorUrl());
        assertNotNull(item.getDescriptorDom());

        Node body = item.getDescriptorDom().selectSingleNode("/descriptor/body");
        assertNotNull(body);
        assertEquals("Crafter Software", body.getText());
    }

    private static class ItemComparator implements Comparator<Item> {

        public static final ItemComparator INSTANCE = new ItemComparator();

        @Override
        public int compare(Item item1, Item item2) {
            return item1.getName().compareTo(item2.getName());
        }

    }

}
