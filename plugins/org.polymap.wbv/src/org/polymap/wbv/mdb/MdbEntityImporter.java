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
package org.polymap.wbv.mdb;

import java.util.HashMap;
import java.util.Map;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * Importiert Spalten aus einer Tabelle in ein {@link Composite}.
 * 
 * @see ImportTabel 
 * @see ImportColumn
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdbEntityImporter<T extends Composite> {

    private static Log log = LogFactory.getLog( MdbEntityImporter.class );
    
    private UnitOfWork              uow;
    
    private Class<T>                entityClass;

    private String                  tableName;
    
    private Map<String,Field>       fieldMap = new HashMap();

    
    public MdbEntityImporter( UnitOfWork uow, Class<T> entityClass ) {
        this.uow = uow;
        this.entityClass = entityClass;
        this.tableName = entityClass.getAnnotation( ImportTable.class ).value();
        
        for (Field f : entityClass.getFields()) {
            ImportColumn a = f.getAnnotation( ImportColumn.class );
            if (a != null) {
                fieldMap.put( a.value(), f );
            }
        }
    }
    
    
    public String getTableName() {
        return tableName;
    }


    public T createEntity( final Map<String,Object> row ) {
        return (T)uow.createEntity( (Class<Entity>)entityClass, null, new ValueInitializer<Entity>() {
            @Override
            public Entity initialize( Entity proto ) throws Exception {
                return (Entity)fill( (T)proto, row );
            }
        });
    }
    

    public T fill( T composite, final Map<String,Object> row ) {
        for (Map.Entry<String,Object> entry : row.entrySet()) {
            Field f = fieldMap.get( entry.getKey() );
            if (f != null) {
                try {
                    //log.info( "    " +  );
                    Property prop = (Property)f.get( composite );
                    if (entry.getValue() != null) {
                        prop.set( entry.getValue() );
                    }
                }
                catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException( e );
                }
            }
        }
        return composite;
    }
    
}