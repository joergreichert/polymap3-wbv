/*
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.wbv.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.data.ui.featuretable.IFeatureContentProvider;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.query.Query;
import org.polymap.core.model2.runtime.PropertyInfo;

/**
 * Used to display {@link Entity} collections as result of a {@link Query}, or the
 * contents of a collection property that contains {@link Composite} instances.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class CompositesFeatureContentProvider
        implements IFeatureContentProvider {

    private static Log log = LogFactory.getLog( CompositesFeatureContentProvider.class );

    private Iterable<? extends Composite>   composites;


    public CompositesFeatureContentProvider( Iterable<? extends Composite> composites ) {
        this.composites = composites;
    }


    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        this.composites = (Iterable<? extends Composite>)newInput;
    }


    public Object[] getElements( Object input ) {
        log.debug( "getElements(): input=" + input.getClass().getName() );
        List<IFeatureTableElement> result = new ArrayList( 1024 );
        for (final Composite composite : composites) {
            result.add( new FeatureTableElement( composite ) );
        }
        return result.toArray();
    }

    
    public void dispose() {
    }


    /**
     *
     */
    public class FeatureTableElement
            implements IFeatureTableElement {

        private Composite       composite;


        protected FeatureTableElement( Composite composite ) {
            this.composite = composite;
        }
        
        public Composite getComposite() {
            return composite;
        }

        public Object getValue( String name ) {
            try {
                PropertyInfo propInfo = composite.info().getProperty( name );
                return propInfo != null ? ((Property)propInfo.get( composite )).get() : null;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }

        public void setValue( String name, Object value ) {
            try {
                ((Property)composite.info().getProperty( name ).get( composite )).set( value );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }

        public String fid() {
            if (composite instanceof Entity) {
                return (String)((Entity)composite).id();
            }
            else {
                throw new RuntimeException( "Don't know how to build fid out of: " + composite );
            }
        }

    }

}