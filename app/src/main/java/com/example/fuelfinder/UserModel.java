/**
 * Name: UserModel.java
 * Purpose: Define a user class whose variables are added as fields in Firestore document
 */

package com.example.fuelfinder;

public class UserModel {
    private String name;            // Name of the user
    private String email;           // E-mail of the user

    /**
     * Constructor to assign name and e-mail of the user to the private variables
     */
    public UserModel(String name, String email){
        this.name = name;
        this.email = email;
    }

    /**
     * Default Constructor to handle any exceptions
     */
    public UserModel() {
    }

    /**
     * Getter Method to return the name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * Setter Method to set the name of the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter Method to get the e-mail of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter Method to set the e-mail of the user
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
