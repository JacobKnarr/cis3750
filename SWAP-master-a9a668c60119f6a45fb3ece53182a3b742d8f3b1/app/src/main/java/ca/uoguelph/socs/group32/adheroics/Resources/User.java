package ca.uoguelph.socs.group32.adheroics.Resources;

/**
 * Created by justin on 2017-11-21.
 */

public class User {
    private String first_name;
    private String last_name;
    private String username;
    private String email;
    private String phone_number;
    public User(String first_name, String last_name, String username, String email, String phone_number){
        super();
        setFirstName(first_name);
        setLastName(last_name);
        setUsername(username);
        setEmail(email);
        setPhoneNumber(phone_number);
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public void setPhoneNumber(String phone_number) {
        this.phone_number = phone_number;
    }
}
