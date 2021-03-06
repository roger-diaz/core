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
package org.craftercms.core.xml.mergers;

import org.dom4j.Document;
import org.craftercms.core.exception.XmlMergeException;

import java.util.List;

/**
 * Merges a set of XML DOM descriptors into a new DOM.
 *
 * @author Alfonso Vásquez
 */
public interface DescriptorMerger {

    /**
     * Merges a set of XML DOM descriptors into a new DOM.
     *
     * @param descriptorsToMerge
     *          the XML DOMs of the descriptors to merge
     * @return the result of the merging
     * @throws XmlMergeException
     */
    public Document merge(List<Document> descriptorsToMerge) throws XmlMergeException;

}
