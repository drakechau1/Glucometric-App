package com.example.glucometric1;

public class User {
    String Name;
    String Age;
    String Timestamp;
    String DeviceID;
    String Glucose;
    String Gender;
    String Id;

    public User(String name, String age, String timestamp, String deviceID, String glucose, String gender, String id) {
        Name = name;
        Age = age;
        Timestamp = timestamp;
        DeviceID = deviceID;
        Glucose = glucose;
        Gender = gender;
        Id    = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public String getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(String deviceID) {
        DeviceID = deviceID;
    }

    public String getGlucose() {
        return Glucose;
    }

    public void setGlucose(String glucose) {
        Glucose = glucose;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                "Name='" + Name + '\'' +
                ", Age='" + Age + '\'' +
                ", Timestamp='" + Timestamp + '\'' +
                ", DeviceID='" + DeviceID + '\'' +
                ", Glucose='" + Glucose + '\'' +
                ", Gender='" + Gender + '\'' +
                ", Id='" + Id + '\'' +
                '}';
    }
}
