package org.example;

import javax.sql.RowSet;
import java.io.File;
import java.net.URL;
import java.sql.*;


/**
 * Class to allow a way of managing the database and how it is handled
 */
public class databaseHandler {
    // Location of the database
    private String url = "jdbc:ucanaccess://src/main/resources/TheDVD_DB.accdb";
    private ResultSet rs;
    private Connection c;
    public databaseHandler() {
        try {

            // Load UcanaccessDriver
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            // Connect to the database
            this.c = DriverManager.getConnection(this.url);
        }catch(Exception ex){
            ex.printStackTrace();

        }
    }


    /** Get the result set based on the parameters
     *
     * @param orderReq what collumn to order by
     * @param x what title or upc to order by
     * @param mode determine whether to search for title, upcuid, etc.
     * @return requested ResultSet
     */
    public ResultSet getResultSet(String orderReq, String x, int mode) {
        // Get order request
        String order;

        // If mode == 0, order by title
        if (mode==0) {
            // Create the specific wording desired
            if (!x.equals("")){
                order = "where title like " + "\"*"+ x + "*\""+ " order by " + orderReq;
            }
            else {
                order = "order by " + orderReq;
            }
        }
        // If mode == 1, order by uuid (upc)
        else {
            // create specific wording desired
            if (!x.equals("")){
                order = "where uuid like " + "\"*"+ x + "*\""+ " order by " + orderReq;
                System.out.println(order);
            }
            else {
                order = " order by " + orderReq;
            }
        }


        try{
            // Create prepared statement, execute it, close it
            PreparedStatement pStatement = this.c.prepareStatement("select * from Collection " + order);
            ResultSet resultSet = pStatement.executeQuery();
            System.out.println("Returning result set");
            pStatement.close();

            // Update database handler's current result set and return the result set
            this.rs = resultSet;
            return resultSet;
       } catch (java.sql.SQLException ex){
            System.out.println("Printing SQL Exception at databaseHandler" + ex.getMessage());
            return null;
       }
   }

    public ResultSet getResultSet(String orderReq, String title){
        return getResultSet(orderReq, title, 0);
    }


    /**
     *
     * @param upc code for dvd
     * @param title for dvd
     * @param cat genre for dvd
     * @param runtime for dvd
     * @param release date for dvd
     */
    public void addTitle(String upc, String title, String cat, int runtime, String release){
        try {
            // Set up the prepared statement and then execute it
            PreparedStatement pStatement = this.c.prepareStatement("insert into Collection values(?,?,?,?,?)");


            pStatement.setString(5,upc);
            pStatement.setString(1,title);
            pStatement.setString(2, cat);
            pStatement.setInt(3,runtime);
            pStatement.setString(4, release);


            pStatement.executeUpdate();

            // close it
            pStatement.close();


        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void deleteTitle(String upc){
        try {
            // Set up prepared statement and execute it

            System.out.println("delete from Collection where UUID="+"\""+upc+"\"");
            PreparedStatement pStatement = this.c.prepareStatement("delete from Collection where UUID="+"\""+upc+"\"");
            pStatement.executeUpdate();
            // Close statement
            pStatement.close();


        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
