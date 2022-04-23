/*
Name: @OrangeCoding2021
Date Completed: 4/22/2022
Purpose: A functioning program to allow a user to manage a DVD Collection through a GUI.
 */

package org.example;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.EventObject;


/**
 * JavaFX App
 */
public class App extends Application {

    // Create textfields
    private TextField movieNameTF = new TextField();
    private TextField movieCatTF = new TextField();
    private TextField movieLengthTF = new TextField();
    private TextField movieReleaseTF = new TextField();
    private TextField movieUUIDTF = new TextField();
    private TextField movieRatingTF = new TextField("0");

    // Create Labels
    private Label movieNameLabel = new Label("Movie Name:\t");
    private Label movieCatLabel = new Label("Movie Category:\t");
    private Label movieLengthLabel = new Label("Movie Length: \t");
    private Label movieReleaseLabel = new Label("Movie Release Date:\t");
    private Label movieUUIDLabel = new Label("DVD UPC:\t");
    private Label movieRatingLabel = new Label("Rating:\t");
    private Label errorLabel = new Label("");

    // Create Buttons
    private Button addMovieButton = new Button("Add a Movie");
    private Button searchButton = new Button("Search");
    private Button addButton = new Button("Add");
    private Button mainButton = new Button("Main ");

    // Ordering Buttons
    private Button orderMovieButton = new Button("Movie");
    private Button orderUUIDButton = new Button("UPC");
    private Button orderCategoryButton = new Button("Category");
    private Button orderRuntimeButton = new Button("Mins");
    private Button orderReleaseButton = new Button("Year");
    private Button nextButton = new Button("Next");
    private Button prevButton = new Button("Prev");
    private Button reloadButton = new Button("Reload");

    // Default variables
    private String orderReq = "Title";
    private String searchTerm = "";
    private boolean canNextPage = false;
    private boolean canPrevPage = false;
    private boolean canRSNext = false;


    // Default mode. Used to determine if title or UPC is being searched for
    int mode = 0;

    // Create object to handle database
    private databaseHandler db = new databaseHandler();

    // Page Number Variables
    private int numStart = 1;
    private int pageNumEnd= 10;

    // Initialize results found
    boolean resultsFound = false;
    // Initialize num
    int num = 0;
    // Initialize entryNum that helps get the result set pointer to the right spot
    int entryNum = 1;

    // Variable to access and pass the stage
    private Stage currentStage;

    // Tell if result set is empty or not
    private boolean firstExists = false;




    public Stage getCurrentStage() {
        return currentStage;
    }

    @Override
    public void start(Stage stage) {

        // Set the width of the stage and make it not resizable
        stage.setWidth(700);
        stage.setResizable(false);
        // Set title
        stage.setTitle("TheDVDCollector v1.0.0");

        // Initialize count variable
        int count = 5;

        // Set currentStage to the stage
        currentStage = stage;

        // Create pane
        GridPane mainPane = new GridPane();


        // Set gaps and column widths
        mainPane.setHgap(3);
        mainPane.setVgap(3);
        mainPane.getColumnConstraints().add(new ColumnConstraints(300));
        mainPane.getColumnConstraints().add(new ColumnConstraints(100));
        mainPane.getColumnConstraints().add(new ColumnConstraints(50));
        mainPane.getColumnConstraints().add(new ColumnConstraints(50));
        mainPane.getColumnConstraints().add(new ColumnConstraints(100));

        // Set alignment
        mainPane.setAlignment(Pos.CENTER);

        // Construct layout
        mainPane.add(movieNameTF, 0, 0);
        mainPane.add(searchButton,1,0);
        mainPane.add(addMovieButton, 4, 0);
        mainPane.add(orderUUIDButton,4,4);
        mainPane.add(orderMovieButton,0,4);
        mainPane.add(orderCategoryButton,1,4);
        mainPane.add(orderRuntimeButton,2,4);
        mainPane.add(orderReleaseButton,3,4);
        mainPane.add(prevButton,3, 16);
        mainPane.add(nextButton,4, 16);
        mainPane.add(reloadButton,0,16);

        // Add functionality to reload button
        reloadButton.setOnAction(e->{
            // Reload everything to default values then refresh the page
            searchTerm = "";
            numStart = 1;
            pageNumEnd= 10;
            orderReq = "Title";
            refresh(stage);
        });

        // Add functionality to Search button
        searchButton.setOnAction(e->{
            // Set page number variables back to default
            numStart = 1;
            pageNumEnd= 10;
            // If no search term entered just refresh the page
            searchTerm = movieNameTF.getText();
            if (!(searchTerm == "" )) {
                searchTerm = searchTerm;
            }

           refresh(stage);
        });

        // Left and right page buttons
        nextButton.setOnAction(e -> {
            // If there is results for another page, prepare to go to the next page
            if (canRSNext) {
                pageNumEnd += 10;
                numStart += 10;
                refresh(stage);
            }
        });
        prevButton.setOnAction(e-> {
            // Check to see if there is a previous page, if so, prepare to go to the previous page
            if (canPrevPage) {
                pageNumEnd -= 10;
                numStart -= 10;
                refresh(stage);
            }
        });

        // Set the actions the sorting buttons will do
        orderMovieButton.setOnAction(e -> {
            orderReq = "Title";
            refresh(stage);
        });
        orderUUIDButton.setOnAction(e -> {
            orderReq = "UUID";
            refresh(stage);
        });
        orderCategoryButton.setOnAction(e -> {
            orderReq = "Category";
            refresh(stage);
        });
        orderRuntimeButton.setOnAction(e -> {
            orderReq = "Runtime";
            refresh(stage);
        });
        orderReleaseButton.setOnAction(e -> {
            orderReq = "Release";
            refresh(stage);
        });
        // Add functionality to addMovieButton
        addMovieButton.setOnAction(e->{
            switchToAddMovie(stage);

        }  );


        // Try and catch for SQL Exception
        try {

            // Set num to 0 and resultsFound to false
            num = 0;
            resultsFound = false;

            // Set entryNum to 0
            entryNum = 0;
            // Get result set

            ResultSet rs = db.getResultSet(orderReq, searchTerm,0);



            // Increment counter by 1
            count +=1;


            // Make sure the result set cursor is currently at the right spot
            while (entryNum+1 < numStart) {
                rs.next();
                entryNum +=1;
            }
            // Move the pointer to right spot
            canRSNext = rs.next();
            // Once it is at the right spot, loop through the entries for the current page
            for (num=numStart;num<=pageNumEnd && canRSNext;num++) {
                // Set results Found to true to signify that there are items displayed
                resultsFound = true;

                // Display the infromation
                mainPane.add(new Label((num)+". " + rs.getString(1)),0,count);
                mainPane.add(new Label(rs.getString(2)),1,count);
                mainPane.add(new Label(rs.getString(3)),2,count);
                mainPane.add(new Label(rs.getString(4)),3,count);
                mainPane.add(new Label(rs.getString(5)),4,count);

                // Create a delete button
                buttonInfo localButton = new buttonInfo("X",(rs.getString(5)));


                // Set the action to perform when clicked
                localButton.setOnAction(e->{

                    // Disable button
                    localButton.setDisable(true);
                    localButton.setVisible(false);
                    // Delete then refresh the stage
                    db.deleteTitle(localButton.getDvdAssociation());
                    refresh(stage);
                });
                // Add the button
                mainPane.add(localButton,7,count);
                // Increment count
                count +=1;


                canRSNext = rs.next();

            }
            // See if there is a previous page
            if (numStart>1) {
                canPrevPage = true;
            }
            else {
                canPrevPage = false;
            }
            // If no results found, display so to the user
            if (!resultsFound) {
                mainPane.add(new Label("Oops " + movieNameTF.getText() + " was not found!" +
                        " Try internet."),0,10);

            }
            // If more pages, show next buton
            if (canRSNext) {
                nextButton.setVisible(true);
            }
            else {
                nextButton.setVisible(false);
            }
            // If previous pages, show prev button
            if (canPrevPage) {
                prevButton.setVisible(true);
            }
            else {
                prevButton.setVisible(false);
            }

        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
        }



        // Set the Scene and stage
        Scene mainScene = new Scene(mainPane, 700, 500);
        stage.setScene(mainScene);
        stage.show();

    }

    // "Refresh" the page
    public void refresh(Stage stage) {
        start(stage);
    }


    /**
     * Switch to the Add Movie part of the program
     * @param stage
     */
    public void switchToAddMovie(Stage stage) {
        // Create pane
        GridPane addMoviePane = new GridPane();
        // Set alignment and such
        addMoviePane.setAlignment(Pos.CENTER);
        // Set gaps
        addMoviePane.setHgap(4);
        addMoviePane.setVgap(7);

        // Set title of window
        stage.setTitle("TheDVDCollector - Add a Movie");

        // Add textboxes
        addMoviePane.add(movieNameLabel, 0, 0);
        addMoviePane.add(movieNameTF, 1, 0);
        addMoviePane.add(movieCatLabel, 0, 1);
        addMoviePane.add(movieCatTF, 1, 1);
        addMoviePane.add(movieLengthLabel, 0, 2);
        addMoviePane.add(movieLengthTF, 1, 2);
        addMoviePane.add(movieReleaseLabel, 0, 3);
        addMoviePane.add(movieReleaseTF, 1, 3);
        addMoviePane.add(movieUUIDLabel,0,4);
        addMoviePane.add(movieUUIDTF,1,4);

        // Add buttons
        addMoviePane.add(addButton, 0, 5);
        addMoviePane.add(mainButton,1,5);

        // Main button to bring user back to main page
        mainButton.setOnAction(e -> start(stage));
        // Add error label
        addMoviePane.add(errorLabel,1,6);
        // Button adjustments
        addButton.setOnAction(e -> {


            try {
                // Get the result set and see if the attempted UPC code is in the database already
                ResultSet rs = db.getResultSet(orderReq, movieUUIDTF.getText(),1);
                firstExists = rs.next();
                rs.close();
                // Put the result set back to the last user requested one
                rs = db.getResultSet(orderReq, searchTerm,0);

            }
            catch (SQLException ex) {
                ex.printStackTrace();

            }

            // If a dvd with the same upc code was found or error code, let the user know they need to change the code
            if (firstExists || movieUUIDTF.getText().equals("Must Have Unique UPC")) {
                System.out.println("MUST HAVE UNIQUE UPC");
                movieUUIDTF.setText("Must Have Unique UPC");
                errorLabel.setText("Unsuccessful!");
            }
            else {
                // Add default values for spaces left blank
                System.out.println("Attempting to add...");
                if (movieNameTF.getText() == "") {
                    movieNameTF.setText("Unknown");
                }
                if (movieCatTF.getText() == "") {
                    movieCatTF.setText("Unknown");
                }
                if (movieLengthTF.getText() == "") {
                    movieLengthTF.setText("-1");
                }
                if (movieReleaseTF.getText() == "") {
                    movieReleaseTF.setText("Unknown");
                }

                try {
                    // See if the movie length is a integer
                    int moveLength = Integer.parseInt(movieLengthTF.getText());
                    try {
                        // Try adding the movie to database if the movie length is a integer.
                        db.addTitle(movieUUIDTF.getText(),
                                movieNameTF.getText(), movieCatTF.getText(), Integer.parseInt(movieLengthTF.getText()), movieReleaseTF.getText());
                        System.out.println("Successfully added movie to database");
                        errorLabel.setText("Successfully added " + movieNameTF.getText());

                    }
                    catch (Exception ex){
                        ex.printStackTrace();

                    }
                }
                catch (Exception ex) {
                    // If its not, let the user know
                    movieLengthTF.setText("Must be a integer");
                    errorLabel.setText("Unsuccessful!");
                }


            }

            }
            );




        // Create the scene and show it
        Scene addMovieScene = new Scene(addMoviePane, 700, 500);
        stage.setScene(addMovieScene);
        stage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }

}
