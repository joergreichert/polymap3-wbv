/*
 * Copyright (C) 2014 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.wbv.model;

import static org.polymap.core.model2.query.Expressions.is;
import static org.polymap.core.model2.query.Expressions.template;

import com.google.common.collect.Iterables;

import org.polymap.core.model2.CollectionProperty;
import org.polymap.core.model2.Defaults;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.MinOccurs;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.query.Query;
import org.polymap.core.model2.store.feature.SRS;

import org.polymap.wbv.mdb.ImportColumn;
import org.polymap.wbv.mdb.ImportTable;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SRS("EPSG:4326")
@ImportTable("Waldbesitzer")
public class Waldbesitzer
        extends Entity {

    public enum Waldeigentumsart {
        /** Staatswald (Land Sachsen, Bund) */
        Staat,
        /** Kommunen, Kirchen, Vereine */
        Körperschaft,
        /** Kirchen */
        Kirche,
        /** Privates Eigentum (Einzelpersonen, Unternehmen) */
        Privat
    }

    public Property<Waldeigentumsart>   eigentumsArt;

    @Defaults
    @ImportColumn("WBS_Bemerkung")
    public Property<String>             bemerkung;

    @Defaults
    @ImportColumn("WBS_Pächter")
    public Property<Boolean>            pächter;

    @Defaults
    @ImportColumn("WBS_Papierkorb")
    public Property<Boolean>            gelöscht;

    /** Alle Ansprechpartner, inklusive des {@link #besitzer()}s auf Index 0. */
    @MinOccurs(1)
    public CollectionProperty<Kontakt>  kontakte;

    @Defaults
    public Property<Integer>            besitzerIndex;


    public Kontakt besitzer() {
        return Iterables.get( kontakte, besitzerIndex.get(), null );
    }


    /**
     * Andere Seite der {@link Waldstueck#waldbesitzer} Assoziation.
     */
    public Query<Waldstueck> waldstuecke() {
        Waldstueck wanted = template( Waldstueck.class, context.getRepository() );
        return context.getUnitOfWork().query( Waldstueck.class )
                .where( is( wanted.waldbesitzer, this ) );
    }

}