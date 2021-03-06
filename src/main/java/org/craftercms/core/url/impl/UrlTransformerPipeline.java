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
package org.craftercms.core.url.impl;

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.core.exception.UrlTransformationException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Context;
import org.craftercms.core.url.UrlTransformer;
import org.craftercms.core.exception.UrlTransformationException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Context;
import org.craftercms.core.url.UrlTransformer;

import java.util.Arrays;
import java.util.List;

/**
 * Class description goes HERE
 *
 * @author Sumer Jabri
 * @author Alfonso Vásquez
 */
public class UrlTransformerPipeline implements UrlTransformer {

    private List<UrlTransformer> transformers;

    public UrlTransformerPipeline() {
    }

    public UrlTransformerPipeline(List<UrlTransformer> transformers) {
        this.transformers = transformers;
    }

    public UrlTransformerPipeline(UrlTransformer... transformers) {
        this.transformers = Arrays.asList(transformers);
    }

    @Override
    public String transformUrl(Context context, CachingOptions cachingOptions, String url) throws UrlTransformationException {
        if (CollectionUtils.isNotEmpty(transformers)) {
            for (UrlTransformer transformer : transformers) {
                url = transformer.transformUrl(context, cachingOptions, url);
            }
        }

        return url;
    }

}
