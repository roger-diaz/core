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
package org.craftercms.core.xml.mergers.impl.strategies;

import org.craftercms.core.xml.mergers.impl.strategies.InheritLevelsMergeStrategy;
import org.junit.Before;
import org.junit.Test;
import org.craftercms.core.service.Context;
import org.craftercms.core.xml.mergers.MergeableDescriptor;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Class description goes HERE
 *
 * @author Alfonso Vásquez
 */
public class InheritLevelsMergeStrategyTest {

    public static final String LEVEL_DESCRIPTOR_NAME = "crafter-level-descriptor.level.xml";

    private static final String PRIMARY_DESCRIPTOR_URL = "/folder/sub-folder/descriptor.xml";
    private static final String ROOT_LEVEL_DESCRIPTOR_URL = "/" + LEVEL_DESCRIPTOR_NAME;
    private static final String FOLDER_LEVEL_DESCRIPTOR_URL = "/folder/" + LEVEL_DESCRIPTOR_NAME;
    private static final String SUB_FOLDER_LEVEL_DESCRIPTOR_URL = "/folder/sub-folder/" + LEVEL_DESCRIPTOR_NAME;

    private InheritLevelsMergeStrategy strategy;
    private Context context;

    @Before
    public void setUp() throws Exception {
        setUpTestContext();
        setUpTestStrategy();
    }

    @Test
    public void testGetDescriptors() throws Exception {
        List<MergeableDescriptor> descriptors = strategy.getDescriptors(context, null, PRIMARY_DESCRIPTOR_URL);
        assertDescriptors(descriptors, false);

        descriptors = strategy.getDescriptors(context, null, PRIMARY_DESCRIPTOR_URL, false);
        assertDescriptors(descriptors, false);

        descriptors = strategy.getDescriptors(context, null, PRIMARY_DESCRIPTOR_URL, true);
        assertDescriptors(descriptors, true);
    }

    private void setUpTestContext() {
        context = mock(Context.class);
    }

    private void setUpTestStrategy() {
        strategy = new InheritLevelsMergeStrategy();
        strategy.setLevelDescriptorFileName(LEVEL_DESCRIPTOR_NAME);
    }

    private void assertDescriptors(List<MergeableDescriptor> descriptors, boolean primaryDescriptorOptional) {
        assertEquals(4, descriptors.size());
        assertEquals(ROOT_LEVEL_DESCRIPTOR_URL, descriptors.get(0).getUrl());
        assertEquals(true, descriptors.get(0).isOptional());
        assertEquals(FOLDER_LEVEL_DESCRIPTOR_URL, descriptors.get(1).getUrl());
        assertEquals(true, descriptors.get(1).isOptional());
        assertEquals(SUB_FOLDER_LEVEL_DESCRIPTOR_URL, descriptors.get(2).getUrl());
        assertEquals(true, descriptors.get(2).isOptional());
        assertEquals(PRIMARY_DESCRIPTOR_URL, descriptors.get(3).getUrl());
        assertEquals(primaryDescriptorOptional, descriptors.get(3).isOptional());
    }

}
