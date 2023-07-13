package com.example.glucometric1;

public class DataHistory {
    private String patient_name;
    private String patient_gender;
    private String patient_timestamps;
    private String patient_age;
    private String patient_glucose_value;

    public DataHistory(String patient_name, String patient_gender, String patient_timestamps, String patient_age, String patient_glucose_value) {
        this.patient_name = patient_name;
        this.patient_gender = patient_gender;
        this.patient_timestamps = patient_timestamps;
        this.patient_age = patient_age;
        this.patient_glucose_value = patient_glucose_value;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public String getPatient_gender() {
        return patient_gender;
    }

    public void setPatient_gender(String patient_gender) {
        this.patient_gender = patient_gender;
    }

    public String getPatient_timestamps() {
        return patient_timestamps;
    }

    public void setPatient_timestamps(String patient_timestamps) {
        this.patient_timestamps = patient_timestamps;
    }

    public String getPatient_glucose_value() {
        return patient_glucose_value;
    }

    public void setPatient_glucose_value(String patient_glucose_value) {
        this.patient_glucose_value = patient_glucose_value;
    }

    public String getPatient_age() {
        return patient_age;
    }

    public void setPatient_age(String patient_age) {
        this.patient_age = patient_age;
    }

    @Override
    public String toString() {
        return "DataHistory{" +
                "patient_name='" + patient_name + '\'' +
                ", patient_gender='" + patient_gender + '\'' +
                ", patient_timestamps='" + patient_timestamps + '\'' +
                ", patient_age='" + patient_age + '\'' +
                ", patient_glucose_value='" + patient_glucose_value + '\'' +
                '}';
    }
}
