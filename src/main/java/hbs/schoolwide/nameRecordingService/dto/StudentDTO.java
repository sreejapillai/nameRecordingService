package hbs.schoolwide.nameRecordingService.dto;

import hbs.common.service.configuration.ConfigurationService;
import hbs.schoolwide.nameRecordingService.common.Constants;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by spillai on 2/23/2017.
 */
public class StudentDTO implements Serializable{

    private static final long serialVersionUID = 3165044083182440917L;

    private Long personId;
    private Long huId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String email;
    private String classSection;
    private String studentStatus;
    private String estimatedGradDate;
    private String studentType;
    private String countryOfOrigin;
    private String recordingNote;
    private Boolean recordingAvailable;
    private String recordingLastModifiedDate;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
    private String fileUploadLocation = ConfigurationService.getInstance().getConfiguration().getString(Constants.NAME_RECORDING_UPLOAD_LOCATION);

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getHuId() {
        return huId;
    }

    public void setHuId(Long huId) {
        this.huId = huId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClassSection() {
        return classSection;
    }

    public void setClassSection(String classSection) {
        this.classSection = classSection;
    }

    public String getStudentStatus() {
        return studentStatus;
    }

    public void setStudentStatus(String studentStatus) {
        this.studentStatus = studentStatus;
    }

    public String getEstimatedGradDate() {
        return estimatedGradDate;
    }

    public void setEstimatedGradDate(String estimatedGradDate) {
        this.estimatedGradDate = estimatedGradDate;
    }

    public String getStudentType() {
        return studentType;
    }

    public void setStudentType(String studentType) {
        this.studentType = studentType;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public String getNameRecordingUrl(){
        checkFileExist();
        StringBuilder result = new StringBuilder();
        if(recordingAvailable) {
            result.append(ConfigurationService.getInstance().getConfiguration().getString(Constants.NAME_RECORDING_FILE_BASE_URL))
                    .append(personId)
                    .append(".wav");

            return result.substring(0, result.length());
        }
        return result.toString();
    }

    public String getRecordingNote() {
        return recordingNote;
    }

    public void setRecordingNote(String recordingNote) {
        this.recordingNote = recordingNote;
    }

    public Boolean getRecordingAvailable() {
        String completeFilePath = fileUploadLocation + personId +".wav";
        return new File(completeFilePath).exists();
    }

    public void setRecordingAvailable(Boolean recordingAvailable) {
        this.recordingAvailable = recordingAvailable;
    }

    public String getRecordingLastModifiedDate() {
        String completeFilePath = fileUploadLocation + personId +".wav";
        if(getRecordingAvailable()){
            return sdf.format(new Date(new File(completeFilePath).lastModified()));
        }
        return this.recordingLastModifiedDate;
    }

    public void setRecordingLastModifiedDate(String recordingLastModifiedDate) {
        this.recordingLastModifiedDate = recordingLastModifiedDate;
    }

    public void checkFileExist(){

        String completeFilePath = fileUploadLocation + personId +".wav";

        recordingAvailable = new File(completeFilePath).exists();
        if(recordingAvailable) {
            recordingLastModifiedDate = sdf.format(new Date(new File(completeFilePath).lastModified()));
        }
    }
}
