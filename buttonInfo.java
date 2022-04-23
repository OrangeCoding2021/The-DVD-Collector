package org.example;
import javafx.scene.control.Button;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.EventObject;
/**
Button Info Class
Extends Button

Adds databaseHandler reference and the dvd it is tied to UPC.
 */
public class buttonInfo extends Button{


    private String dvdAssociation;

    /**
     * Constructor
     *
     * @param x button label
     * @param dvdUPC UPC code to link the corresponding dvd
     */
    public buttonInfo(String x, String dvdUPC){
        // Give the button a label
        super(x);

        // Set association
        this.dvdAssociation = dvdUPC;

    }

    public String getDvdAssociation() {
        return dvdAssociation;
    }

    public void setDvdAssociation(String dvdAssociation) {
        this.dvdAssociation = dvdAssociation;
    }
}
