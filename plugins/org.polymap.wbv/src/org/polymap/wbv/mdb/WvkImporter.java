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

import java.util.Map;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.util.LinkResolver;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.SubMonitor;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.ValueInitializer;
import org.polymap.wbv.WbvPlugin;
import org.polymap.wbv.model.Flurstueck;
import org.polymap.wbv.model.Gemarkung;
import org.polymap.wbv.model.Gemeinde;
import org.polymap.wbv.model.Kontakt;
import org.polymap.wbv.model.Waldbesitzer;
import org.polymap.wbv.model.Waldbesitzer.Waldeigentumsart;

/**
 * Importiert Daten aus WVK_dat.mdb 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WvkImporter
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( WvkImporter.class );
    
    private File            baseDir;

    private UnitOfWork      uow;

    
    public WvkImporter( UnitOfWork uow, File baseDir ) {
        super( "WVK-Daten importieren" );
        this.uow = uow;
        this.baseDir = baseDir;
    }
    
    
    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        try {
            importData( monitor );
            return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "", e );
            return new Status( IStatus.ERROR, WbvPlugin.ID, "", e );
        }
    }


    public void importData( IProgressMonitor monitor ) throws Exception {
        Database db = DatabaseBuilder.open( new File( baseDir, "WVK_dat.mdb" ) );
        try {
            monitor.beginTask( getLabel(), 60 );
            monitor.subTask( "Datenbank öffnen" );
            db.setLinkResolver( new LinkResolver() {
                @Override
                public Database resolveLinkedDatabase( Database linkerDb, String linkeeFileName ) throws IOException {
                    return DatabaseBuilder.open( new File( FilenameUtils.getName( linkeeFileName ) ) );
                }
            });
            monitor.worked( 10 );
   
            // Waldbesitzer
            SubMonitor submon = new SubMonitor( monitor, 10 );
            final MdbEntityImporter<Waldbesitzer> wbImporter = new MdbEntityImporter( uow, Waldbesitzer.class );
            Table table = db.getTable( wbImporter.getTableName() );
            submon.beginTask( "Waldbesitzer", table.getRowCount() );
            for (Row row=table.getNextRow(); row != null && !submon.isCanceled(); row=table.getNextRow()) {
                final Row finalRow = row;
                String id = row.get( "ID_WBS" ).toString();
                @SuppressWarnings("unused")
                Waldbesitzer wb = uow.createEntity( Waldbesitzer.class, "Waldbesitzer."+id, new ValueInitializer<Waldbesitzer>() {
                    @Override
                    public Waldbesitzer initialize( Waldbesitzer proto ) throws Exception {
                        wbImporter.fill( proto, finalRow );
                        
                        String ea = (String)finalRow.get( "WBS_EA" );
                        switch (ea != null ? ea : "null") {
                            case "P": proto.eigentumsArt.set( Waldeigentumsart.Privat ); break;
                            case "K42": proto.eigentumsArt.set( Waldeigentumsart.Kirche ); break;
                            case "C": proto.eigentumsArt.set( Waldeigentumsart.Körperschaft ); break;
                            default : {
                                log.warn( "Unbekannte Eigentumsart: " + ea );
                                proto.eigentumsArt.set( Waldeigentumsart.Unbekannt );
                            }
                        }
                        return proto;
                    }
                });
                //log.info( "   " + wb );
                submon.worked( 1 );
            }
            submon.done();
            
            // Waldbesitzer_Adresse
            new MdbEntityImporter<Kontakt>( uow, Kontakt.class ) {
                @Override
                public String buildId( Row row ) {
                    return null;
                }
                @Override
                public Kontakt createEntity( final Map row, String id ) {
                    String wbId = row.get( "ID_WBS" ).toString();
                    Waldbesitzer wb = uow.entity( Waldbesitzer.class, "Waldbesitzer."+wbId );
                    return wb.kontakte.createElement( new ValueInitializer<Kontakt>() {
                        @Override
                        public Kontakt initialize( Kontakt proto ) throws Exception {
                            return fill( proto, row );
                        }
                    });
                }
            }.importTable( db, monitor );

            // Gemarkung
            new MdbEntityImporter<Gemarkung>( uow, Gemarkung.class ) {
                @Override
                public String buildId( Row row ) {
                    String result = "Gemarkung." + row.get( "ID_Gemarkung" );
                    return result;
                }
            }.importTable( db, monitor );

            // Gemeinde
            new MdbEntityImporter<Gemeinde>( uow, Gemeinde.class ) {
                @Override
                public String buildId( Row row ) {
                    return "Gemeinde." + row.get( "ID_Gemeinde" );
                }
            }.importTable( db, monitor );

            // Flurstücke
            new MdbEntityImporter<Flurstueck>( uow, Flurstueck.class ) {
                @Override
                public String buildId( Row row ) {
                    String id = row.get( "ID_FL" ).toString();
                    return "Flurstueck." + id;                  
                }
                @Override
                public Flurstueck createEntity( final Map row, String id ) {
                    String wbId = row.get( "ID_WBS" ).toString();
                    Waldbesitzer wb = uow.entity( Waldbesitzer.class, "Waldbesitzer."+wbId );
                    return wb.flurstuecke.createElement( new ValueInitializer<Flurstueck>() {
                        @Override
                        public Flurstueck initialize( Flurstueck proto ) throws Exception {
                            fill( proto, row );
                            
                            String gemeindeId = row.get( "FL_Gemeinde" ).toString();
                            Gemeinde gemeinde = uow.entity( Gemeinde.class, "Gemeinde."+gemeindeId );
                            proto.gemeinde.set( gemeinde );

                            String gemarkungId = row.get( "FL_Gemarkung" ).toString();
                            Gemarkung gemarkung = uow.entity( Gemarkung.class, "Gemarkung."+gemarkungId );
                            proto.gemarkung.set( gemarkung );
                            return proto;
                        }
                    });
                }
            }.importTable( db, monitor );

            monitor.done();
        }
        finally {
            if (db != null) { db.close(); }
        }
    }


    @Override
    public IStatus redo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }
    
}
