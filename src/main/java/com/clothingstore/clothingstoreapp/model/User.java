package com.clothingstore.clothingstoreapp.model;

public class User extends BaseEntity {
    private String email;
    private String username;
    private String password;
    private String role;

    public User(int userID, String username, String password, String role) {
        this(userID, null, username, password, role);
    }

    public User(int userID, String email, String username, String password, String role) {
        super(userID);
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = UserRole.fromDbValue(role);
    }

    public int getUserID() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}

