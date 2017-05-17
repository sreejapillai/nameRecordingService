package hbs.schoolwide.nameRecordingService.vo;

import hbs.common.appnaccess.IPerson;
import hbs.common.service.configuration.ConfigurationService;
import hbs.schoolwide.nameRecordingService.common.Constants;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by spillai on 3/24/2017.
 */
public class UserVO implements Serializable {

    private static final long serialVersionUID = -3935582885780735365L;

    private Long personId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private Boolean recordingAvailable;
    private String recordingLastModifiedDate;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

    public UserVO(IPerson person) {
        personId = person.getPersonId();
        firstName = person.getFirstName();
        lastName = person.getLastName();
        email = person.getPrimaryEmailAddress();
        checkFileExist();
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getRecordingAvailable() {
        return recordingAvailable;
    }

    public void setRecordingAvailable(Boolean recordingAvailable) {
        this.recordingAvailable = recordingAvailable;
    }

    public String getRecordingLastModifiedDate() {
        return recordingLastModifiedDate;
    }

    public void setRecordingLastModifiedDate(String recordingLastModifiedDate) {
        this.recordingLastModifiedDate = recordingLastModifiedDate;
    }

    public void checkFileExist(){
        String fileUploadLocation = ConfigurationService.getInstance().getConfiguration().getString(Constants.NAME_RECORDING_UPLOAD_LOCATION);
        String completeFilePath = fileUploadLocation + personId +".wav";

        recordingAvailable = (new File(completeFilePath).exists());
        if(recordingAvailable) {
            recordingLastModifiedDate = sdf.format(new Date(new File(completeFilePath).lastModified()));
        }
    }

    public String getNameRecordingUrl(){
        StringBuilder result = new StringBuilder();
        if(recordingAvailable) {
            result.append(ConfigurationService.getInstance().getConfiguration().getString(Constants.NAME_RECORDING_FILE_BASE_URL))
                    .append(personId)
                    .append(".wav");

            return result.substring(0, result.length());
        }
        return result.toString();
    }
}
