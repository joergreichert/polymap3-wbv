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
package org.polymap.wbv.ui;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NotEmptyValidator;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.wbv.model.Ereignis;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EreignisForm
        extends FormContainer {

    private static Log log = LogFactory.getLog( EreignisForm.class );
    
    private Ereignis                ereignis;
    
    private IPanelSite              panelSite;
    
    private Composite               body;

    private Set<IFormFieldListener> listeners = new HashSet();

    
    public EreignisForm( Ereignis ereignis, IPanelSite panelSite ) {
        this.ereignis = ereignis;
        this.panelSite = panelSite;
    }


    @Override
    public void addFieldListener( IFormFieldListener l ) {
        super.addFieldListener( l );
        listeners.add( l );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite formSite ) {
        body = formSite.getPageBody();
        body.setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 10, 10 ).create() );

//        // geändert
//        new FormFieldBuilder( body, new PropertyAdapter( ereignis.geaendert ) )
//                .setLabel( "Geändert am" )
//                .setField( new StringFormField() )
//                .setValidator( new DateValidator() )
//                .setEnabled( false )
//                .create();

        // Titel
        new FormFieldBuilder( body, new PropertyAdapter( ereignis.titel ) )
                .setValidator( new NotEmptyValidator() ).create().setFocus();

        // Text 
        new FormFieldBuilder( body, new PropertyAdapter( ereignis.text ) )
                .setField( new TextFormField() )
                .setLabel( "" )
                .create().setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 80 ) );
    }
    
}
