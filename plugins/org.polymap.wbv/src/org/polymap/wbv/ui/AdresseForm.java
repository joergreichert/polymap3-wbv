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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.NotEmptyValidator;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.wbv.model.Adresse;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AdresseForm
        extends FormContainer {

    private static Log log = LogFactory.getLog( AdresseForm.class );
    
    private Adresse                 adresse;
    
    private IPanelSite              panelSite;
    
    private Composite               body;

    
    public AdresseForm( Adresse adresse, IPanelSite panelSite ) {
        this.adresse = adresse;
        this.panelSite = panelSite;
    }


    @Override
    public void createFormContent( final IFormEditorPageSite formSite ) {
        body = formSite.getPageBody();
//        if (body.getLayout() == null) {
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 10, 10 ).columns( 1, 1 ).create() );
//        }

        createField( body, new PropertyAdapter( adresse.strasse ) )
                .setLabel( "Strasse" ).setToolTipText( "Strasse und Hausnummer" )
                .setValidator( new NotEmptyValidator() )
                .create();
                //.setLayoutData( FormDataFactory.filled().right( 75 ).create() );

        Composite city = panelSite.toolkit().createComposite( body );
        createField( city, new PropertyAdapter( adresse.plz ) )
                .setLabel( "PLZ / Ort" )
                .setValidator( new NotEmptyValidator() {
                    @Override
                    public String validate( Object fieldValue ) {
                        String result = super.validate( fieldValue );
                        if (result == null) {
                            if (((String)fieldValue).length() != 5) {
                                result = "Postleitzahl muss 5 Stellen haben";
                            }
                            else if (!StringUtils.isNumeric( (String)fieldValue )) {
                                result = "Postleitzahl darf nur Ziffern enthalten";
                            }
                        }
                        return result;
                    }
                })
                .create();

        createField( city, new PropertyAdapter( adresse.ort ) )
                .setLabel( IFormFieldLabel.NO_LABEL )
                .setValidator( new NotEmptyValidator() )
                .create();

        createField( body, new PropertyAdapter( adresse.ortsteil ) )
                .setLabel( "Ortsteil" ).setToolTipText( "Ortsteil" )
                .create();

        createField( body, new PropertyAdapter( adresse.land ) )
                .setLabel( "Land" ).setToolTipText( "Land" )
                .create();
    }

}
