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

import org.craftercms.core.service.Context;
import org.craftercms.core.service.Context;

/**
 * Extension of context that adds properties used by the {@link FileSystemContentStoreAdapter}.
 *
 * @author Alfonso Vásquez
 */
public class FileSystemContext extends Context {

    FileSystemFile rootFolder;

    public FileSystemContext(String id, FileSystemContentStoreAdapter storeAdapter, String storeServerUrl, String rootFolderPath,
                             FileSystemFile rootFolder, boolean cacheOn, int maxAllowedItemsInCache, boolean ignoreHiddenFiles) {
        super(id, storeAdapter, storeServerUrl, rootFolderPath, cacheOn, maxAllowedItemsInCache, ignoreHiddenFiles);

        this.rootFolder = rootFolder;
    }

    public FileSystemFile getRootFolder() {
        return rootFolder;
    }

    @Override
    public String toString() {
        return "FileSystemContext[" +
                "id='" + id + '\'' +
                ", storeAdapter='" + storeAdapter + '\'' +
                ", storeServerUrl='" + storeServerUrl + '\'' +
                ", rootFolderPath='" + rootFolderPath + '\'' +
                ", rootFolder=" + rootFolder +
                ", cacheOn=" + cacheOn +
                ", maxAllowedItemsInCache=" + maxAllowedItemsInCache +
                ", ignoreHiddenFiles=" + ignoreHiddenFiles +
                ']';
    }

}
