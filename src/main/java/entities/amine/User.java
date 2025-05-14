package entities.amine;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.UserRole;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class User {
    private Integer id;
    private String CIN;
    private String Name;
    private String Email;
    private String Password;
    private List<String> roles;
    private String Phone;
    private String Address;
    private boolean isActive;
    private String pathtopic;
    private Date birthday;
    private boolean isVerified;
    private Date accountCreationDate;
    private Date lastLoginDate;
    private int failedLoginAttempts;
    private String bio;
    private Date createdAt;
    private Date updatedAt;
    private String googleId;
    private String avatar;
    private String googleAuthenticatorSecret;
    private boolean isGoogleAuthenticatorEnabled;

    public User() {
        this.isActive = false;
        this.isVerified = false;
        this.failedLoginAttempts = 0;
        this.roles = List.of("ROLE_USER");
        Date now = new Date();
        this.createdAt = now;
        this.accountCreationDate = now;
        this.lastLoginDate = now;
        this.pathtopic = "  ";
    }

    public User(String name, String CIN, String email, String password, String role, String phone, String address, String pathtopic, Date birthday,String bio) {
        Name = name;
        this.CIN = CIN;
        Email = email;
        Password = password;
        this.roles = List.of(role);
        Phone = phone;
        Address = address;
        this.pathtopic = pathtopic;
        this.birthday = birthday;
        this.isActive = false;
        this.isVerified = false;
        this.failedLoginAttempts = 0;
        Date now = new Date();
        this.createdAt = now;
        this.accountCreationDate = now;
        this.lastLoginDate = now;
        this.bio=bio;
    }

    public User(Integer id, String CIN, String name, String email, String password, List<String> roles,
                String phone, String address, boolean isActive, String pathtopic, Date birthday,
                boolean isVerified, Date accountCreationDate, Date lastLoginDate, int failedLoginAttempts,
                String bio, Date createdAt, Date updatedAt, String googleId, String avatar,
                String googleAuthenticatorSecret, boolean isGoogleAuthenticatorEnabled) {
        this.id = id;
        this.CIN = CIN;
        Name = name;
        Email = email;
        Password = password;
        this.roles = roles;
        Phone = phone;
        Address = address;
        this.isActive = isActive;
        this.pathtopic = pathtopic;
        this.birthday = birthday;
        this.isVerified = isVerified;
        this.accountCreationDate = accountCreationDate;
        this.lastLoginDate = lastLoginDate;
        this.failedLoginAttempts = failedLoginAttempts;
        this.bio = bio;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.googleId = googleId;
        this.avatar = avatar;
        this.googleAuthenticatorSecret = googleAuthenticatorSecret;
        this.isGoogleAuthenticatorEnabled = isGoogleAuthenticatorEnabled;
    }

    public User(int id, String cin, String name, String email) {
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPathtopic() {
        return pathtopic;
    }

    public void setPathtopic(String pathtopic) {
        this.pathtopic = pathtopic;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getCIN() {
        return CIN;
    }

    public void setCIN(String CIN) {
        this.CIN = CIN;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

//    public List<String> getRoles() throws JsonProcessingException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        String rolesJson = objectMapper.writeValueAsString(this.roles); // rolesList is a List<String>
//
//        try {
//            return objectMapper.readValue(rolesJson, new TypeReference<List<String>>() {});
//        } catch (IOException e) {
//            e.printStackTrace();
//            return Collections.emptyList(); // Return an empty list in case of error
//        }
//    }
        public List<String> getRoles() {
            return this.roles; // Directly return the roles list
        }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public Date getAccountCreationDate() {
        return accountCreationDate;
    }

    public void setAccountCreationDate(Date accountCreationDate) {
        this.accountCreationDate = accountCreationDate;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGoogleAuthenticatorSecret() {
        return googleAuthenticatorSecret;
    }

    public void setGoogleAuthenticatorSecret(String googleAuthenticatorSecret) {
        this.googleAuthenticatorSecret = googleAuthenticatorSecret;
    }

    public boolean isGoogleAuthenticatorEnabled() {
        return isGoogleAuthenticatorEnabled;
    }

    public void setGoogleAuthenticatorEnabled(boolean googleAuthenticatorEnabled) {
        isGoogleAuthenticatorEnabled = googleAuthenticatorEnabled;
    }
}
