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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.exception.CacheException;
import org.craftercms.core.exception.InvalidContextException;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.impl.AbstractCachedContentStoreAdapter;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.core.exception.CacheException;
import org.craftercms.core.exception.InvalidContextException;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.impl.AbstractCachedContentStoreAdapter;
import org.craftercms.core.util.cache.CacheTemplate;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * REST service that provides several methods to handle Crafter's cache engine.
 *
 * @author Alfonso Vásquez
 * @author hyanghee
 */
@Controller
@RequestMapping(RestControllerBase.REST_BASE_URI + CacheRestController.URL_ROOT)
public class CacheRestController extends RestControllerBase {

    private static final Log logger = LogFactory.getLog(CacheRestController.class);
    
	/** rest URLs **/
    public static final String URL_ROOT = "/cache";
    public static final String URL_CLEAR_ALL_SCOPES = "/clear_all";
    public static final String URL_CLEAR_SCOPE = "/clear";
    public static final String URL_REMOVE_ITEM = "/remove";

    public static final String REQUEST_PARAM_CONTEXT_ID = "contextId";
    public static final String REQUEST_PARAM_URL = "url";

    private CacheTemplate cacheTemplate;
    private ContentStoreService storeService;

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @RequestMapping(value = URL_CLEAR_ALL_SCOPES, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void clearAllScopes() throws CacheException {
        cacheTemplate.getCacheService().clearAll();
        if (logger.isInfoEnabled()) {
        	logger.info("[CACHE] All scopes are cleared.");
        }
     }

    @RequestMapping(value = URL_CLEAR_SCOPE, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void clearScope(@RequestParam(REQUEST_PARAM_CONTEXT_ID) String contextId) throws InvalidContextException, CacheException {
        Context context = storeService.getContext(contextId);
        if (context == null) {
            throw new InvalidContextException("No context found for ID " + contextId);
        }

        cacheTemplate.getCacheService().clearScope(context);
        if (logger.isInfoEnabled()) {
            logger.info("[CACHE] Scope for context " + context + " is cleared.");
        }
    }

    @RequestMapping(value = URL_REMOVE_ITEM, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void removeItem(@RequestParam(REQUEST_PARAM_CONTEXT_ID) String contextId, @RequestParam(REQUEST_PARAM_URL) String url)
            throws InvalidContextException, CacheException {
        Context context = storeService.getContext(contextId);
        if (context == null) {
            throw new InvalidContextException("No context found for ID " + contextId);
        }

        // Content store service always adds a "/" at the beginning before requesting the items from the store adapter, so we need
        // to add it too.
        if (!url.startsWith("/")) {
            url = "/" + url;
        }

        CacheService cacheService = cacheTemplate.getCacheService();
        // Remove all possible cached versions IN STORE ADAPTER. Since cached store service items depend on store adapter items,
        // we don't need to remove them manually.
        cacheService.remove(context, cacheTemplate.getKey(context, url, AbstractCachedContentStoreAdapter.CONST_KEY_ELEM_CONTENT));
        cacheService.remove(context, cacheTemplate.getKey(context, url, true, AbstractCachedContentStoreAdapter.CONST_KEY_ELEM_ITEM));
        cacheService.remove(context, cacheTemplate.getKey(context, url, false, AbstractCachedContentStoreAdapter.CONST_KEY_ELEM_ITEM));
        // In case the item is a folder, remove cached children lists
        cacheService.remove(context, cacheTemplate.getKey(context, url, true, AbstractCachedContentStoreAdapter.CONST_KEY_ELEM_ITEMS));
        cacheService.remove(context, cacheTemplate.getKey(context, url, false, AbstractCachedContentStoreAdapter.CONST_KEY_ELEM_ITEMS));

        if (logger.isInfoEnabled()) {
        	logger.info("[CACHE] removed " + url + " from scope for context " + context);
        }
    }

}
