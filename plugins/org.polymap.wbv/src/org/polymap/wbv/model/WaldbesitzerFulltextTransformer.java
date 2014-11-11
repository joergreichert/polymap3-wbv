/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.wbv.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Association;
import org.polymap.core.model2.Composite;

import org.polymap.rhei.fulltext.model2.EntityFeatureTransformer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WaldbesitzerFulltextTransformer
        extends EntityFeatureTransformer {

    private static Log log = LogFactory.getLog( WaldbesitzerFulltextTransformer.class );

    
    @Override
    protected void visitAssociation( Association prop ) {
        Object value = prop.get();
        if (value instanceof Gemeinde
                || value instanceof Gemarkung) {
            processComposite( (Composite)value );
        }
    }
    
}
