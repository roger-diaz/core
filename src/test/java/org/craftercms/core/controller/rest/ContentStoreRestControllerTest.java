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
package org.craftercms.core.controller.rest;

import org.apache.commons.collections.MapUtils;
import org.craftercms.core.controller.rest.ContentStoreRestController;
import org.craftercms.core.service.*;
import org.junit.Before;
import org.junit.Test;
import org.craftercms.core.exception.AuthenticationException;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.core.processors.ItemProcessor;

import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.core.store.impl.filesystem.FileSystemContentStoreAdapter;
import org.craftercms.core.util.cache.CachingAwareObject;
import org.craftercms.core.util.cache.impl.CachingAwareList;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.craftercms.core.controller.rest.ContentStoreRestController.*;
import static org.craftercms.core.service.CachingOptions.DEFAULT_CACHING_OPTIONS;
import static org.craftercms.core.service.ContentStoreService.UNLIMITED_TREE_DEPTH;
import static org.craftercms.core.service.Context.*;

/**
* Class description goes HERE
*
* @author Alfonso Vásquez
*/
public class ContentStoreRestControllerTest {

    private static final String STORE_TYPE = FileSystemContentStoreAdapter.STORE_TYPE;

    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "1234";

    private static final String LAST_MODIFIED_HEADER_NAME = "Last-Modified";
    private static final String IF_MODIFIED_SINCE_HEADER_NAME = "If-Modified-Since";

    private static final String FOLDER_URL = "/folder";
    private static final String ITEM_URL = FOLDER_URL + "/item";

    private ContentStoreRestController storeRestController;
    private ApplicationContext applicationContext;
    private ContentStoreService storeService;
    private Item item;
    private CachingAwareList<Item> children;
    private Tree tree;
    private Context context;
    private ItemFilter filter;
    private ItemProcessor processor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private WebRequest webRequest;

    @Before
    public void setUp() throws Exception {
        setUpTestContext();
        setUpTestItems();
        setUpTestFilter();
        setUpTestProcessor();
        setUpTestRequest();
        setUpTestResponse();
        setUpTestWebRequest();
        setUpTestApplicationContext();
        setUpTestStoreService();
        setUpTestStoreRestController();
    }

    @Test
    public void testCreateContext() throws Exception {
        ModelMap model = storeRestController.createContext(STORE_TYPE, context.getStoreServerUrl(), USERNAME, PASSWORD,
                context.getRootFolderPath(), context.isCacheOn(), context.getMaxAllowedItemsInCache(), context.ignoreHiddenFiles())
                .getModelMap();
        assertEquals(context.getId(), model.get(MODEL_ATTR_CONTEXT_ID));

        verify(storeService).createContext(STORE_TYPE, context.getStoreServerUrl(), USERNAME, PASSWORD, context.getRootFolderPath(),
                context.isCacheOn(), context.getMaxAllowedItemsInCache(), context.ignoreHiddenFiles());
    }

    @Test
    public void testDestroyContext() throws Exception {
        storeRestController.destroyContext(context.getId());

        verify(storeService).destroyContext(eq(context));
    }

    @Test
    public void testGetItemNotModified() throws Exception {
        testNotModified(item, new RestMethodCallback() {
            @Override
            public ModelMap executeMethod() throws Exception {
                return storeRestController.getItem(webRequest, response, context.getId(), DEFAULT_CACHING_OPTIONS.doCaching(),
                        DEFAULT_CACHING_OPTIONS.getExpireAfter(), DEFAULT_CACHING_OPTIONS.getRefreshFrequency(), ITEM_URL, "processor")
                        .getModelMap();
            }
        });

        verify(storeService).getItem(context, DEFAULT_CACHING_OPTIONS, ITEM_URL, processor);
    }

    @Test
    public void testGetItemModified() throws Exception {
        testModified(item, MODEL_ATTR_ITEM, new RestMethodCallback() {
            @Override
            public ModelMap executeMethod() throws Exception {
                return storeRestController.getItem(webRequest, response, context.getId(), DEFAULT_CACHING_OPTIONS.doCaching(),
                        DEFAULT_CACHING_OPTIONS.getExpireAfter(), DEFAULT_CACHING_OPTIONS.getRefreshFrequency(), ITEM_URL, "processor")
                        .getModelMap();
            }
        });

        verify(storeService).getItem(context, DEFAULT_CACHING_OPTIONS, ITEM_URL, processor);
    }

    @Test
    public void testGetChildrenNotModified() throws Exception {
        testNotModified(children, new RestMethodCallback() {
            @Override
            public ModelMap executeMethod() throws Exception {
                return storeRestController.getChildren(webRequest, response, context.getId(), DEFAULT_CACHING_OPTIONS.doCaching(),
                        DEFAULT_CACHING_OPTIONS.getExpireAfter(), DEFAULT_CACHING_OPTIONS.getRefreshFrequency(), FOLDER_URL, "filter",
                        "processor").getModelMap();
            }
        });

        verify(storeService).getChildren(context, DEFAULT_CACHING_OPTIONS, FOLDER_URL, filter, processor);
    }

    @Test
    public void testGetChildrenModified() throws Exception {
        testModified(children, MODEL_ATTR_CHILDREN, new RestMethodCallback() {
            @Override
            public ModelMap executeMethod() throws Exception {
                return storeRestController.getChildren(webRequest, response, context.getId(), DEFAULT_CACHING_OPTIONS.doCaching(),
                        DEFAULT_CACHING_OPTIONS.getExpireAfter(), DEFAULT_CACHING_OPTIONS.getRefreshFrequency(), FOLDER_URL, "filter",
                        "processor").getModelMap();
            }
        });

        verify(storeService).getChildren(context, DEFAULT_CACHING_OPTIONS, FOLDER_URL, filter, processor);
    }

    @Test
    public void testGetTreeNotModified() throws Exception {
        testNotModified(tree, new RestMethodCallback() {
            @Override
            public ModelMap executeMethod() throws Exception {
                return storeRestController.getTree(webRequest, response, context.getId(), DEFAULT_CACHING_OPTIONS.doCaching(),
                        DEFAULT_CACHING_OPTIONS.getExpireAfter(), DEFAULT_CACHING_OPTIONS.getRefreshFrequency(), FOLDER_URL,
                        UNLIMITED_TREE_DEPTH, "filter", "processor").getModelMap();
            }
        });

        verify(storeService).getTree(context, DEFAULT_CACHING_OPTIONS, FOLDER_URL, UNLIMITED_TREE_DEPTH, filter, processor);
    }

    @Test
    public void testGetTreeModified() throws Exception {
        testModified(tree, MODEL_ATTR_TREE, new RestMethodCallback() {
            @Override
            public ModelMap executeMethod() throws Exception {
                return storeRestController.getTree(webRequest, response, context.getId(), DEFAULT_CACHING_OPTIONS.doCaching(),
                        DEFAULT_CACHING_OPTIONS.getExpireAfter(), DEFAULT_CACHING_OPTIONS.getRefreshFrequency(), FOLDER_URL,
                        UNLIMITED_TREE_DEPTH, "filter", "processor").getModelMap();
            }
        });

        verify(storeService).getTree(context, DEFAULT_CACHING_OPTIONS, FOLDER_URL, UNLIMITED_TREE_DEPTH, filter, processor);
    }

    @Test
    public void testHandleAuthenticationException() throws Exception {
        AuthenticationException ex = new AuthenticationException();

        ModelMap model = storeRestController.handleAuthenticationException(ex).getModelMap();
        assertSame(ex, model.get(EXCEPTION_MODEL_ATTRIBUTE_NAME));
    }

    @Test
    public void testHandlePathNotFoundException() throws Exception {
        PathNotFoundException ex = new PathNotFoundException();

        ModelMap model = storeRestController.handlePathNotFoundException(ex).getModelMap();
        assertSame(ex, model.get(EXCEPTION_MODEL_ATTRIBUTE_NAME));
    }

    @Test
    public void testHandleException() throws Exception {
        Exception ex = new Exception();

        ModelMap model = storeRestController.handleException(ex).getModelMap();
        assertSame(ex, model.get(EXCEPTION_MODEL_ATTRIBUTE_NAME));
    }

    private void testNotModified(CachingAwareObject cachingAwareObject, RestMethodCallback callback) throws Exception {
        cachingAwareObject.setCachingTime(System.currentTimeMillis());
        request.addHeader(IF_MODIFIED_SINCE_HEADER_NAME, cachingAwareObject.getCachingTime());

        ModelMap model = callback.executeMethod();
        assertTrue(MapUtils.isEmpty(model));
        assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatus());
        assertEquals(MUST_REVALIDATE_HEADER_VALUE, response.getHeader(CACHE_CONTROL_HEADER_NAME));
    }

    private void testModified(CachingAwareObject cachingAwareObject, String modelAttributeName, RestMethodCallback callback)
            throws Exception {
        request.addHeader(IF_MODIFIED_SINCE_HEADER_NAME, System.currentTimeMillis());

        Thread.sleep(1000);

        cachingAwareObject.setCachingTime(System.currentTimeMillis());

        ModelMap model = callback.executeMethod();
        assertEquals(cachingAwareObject, model.get(modelAttributeName));
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals(cachingAwareObject.getCachingTime(), new Long(response.getHeader(LAST_MODIFIED_HEADER_NAME)));
        assertEquals(MUST_REVALIDATE_HEADER_VALUE, response.getHeader(CACHE_CONTROL_HEADER_NAME));
    }

    private void setUpTestItems() {
        item = new Item();
        children = new CachingAwareList<Item>(new ArrayList<Item>());
        tree = new Tree();
    }

    private void setUpTestContext() {
        ContentStoreAdapter storeAdapter = mock(ContentStoreAdapter.class);

        context = new Context("0", storeAdapter, "http://localhost:8080", "/", DEFAULT_CACHE_ON, DEFAULT_MAX_ALLOWED_ITEMS_IN_CACHE,
                DEFAULT_IGNORE_HIDDEN_FILES);
    }

    private void setUpTestFilter() {
        filter = mock(ItemFilter.class);
    }

    private void setUpTestProcessor() {
        processor = mock(ItemProcessor.class);
    }

    private void setUpTestRequest() {
        request = new MockHttpServletRequest();
        request.setMethod("GET");
    }

    private void setUpTestResponse() {
        response = new MockHttpServletResponse();
    }

    private void setUpTestWebRequest() {
        webRequest = new ServletWebRequest(request, response);
    }

    private void setUpTestApplicationContext() {
        applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("filter", ItemFilter.class)).thenReturn(filter);
        when(applicationContext.getBean("processor", ItemProcessor.class)).thenReturn(processor);
    }

    private void setUpTestStoreService() {
        storeService = mock(ContentStoreService.class);
        try {
            when(storeService.createContext(STORE_TYPE, context.getStoreServerUrl(), USERNAME, PASSWORD, context.getRootFolderPath(),
                    context.isCacheOn(), context.getMaxAllowedItemsInCache(), context.ignoreHiddenFiles())).thenReturn(context);
            when(storeService.getContext(context.getId())).thenReturn(context);
            when(storeService.getItem(context, DEFAULT_CACHING_OPTIONS, ITEM_URL, processor)).thenReturn(item);
            when(storeService.getChildren(context, DEFAULT_CACHING_OPTIONS, FOLDER_URL, filter, processor)).thenReturn(children);
            when(storeService.getTree(context, DEFAULT_CACHING_OPTIONS, FOLDER_URL, UNLIMITED_TREE_DEPTH, filter, processor)).thenReturn(
                    tree);
        } catch (Exception e) {
        }
    }

    private void setUpTestStoreRestController() {
        storeRestController = new ContentStoreRestController();
        storeRestController.setApplicationContext(applicationContext);
        storeRestController.setStoreService(storeService);
    }

    private interface RestMethodCallback {

        ModelMap executeMethod() throws Exception;

    }

}
