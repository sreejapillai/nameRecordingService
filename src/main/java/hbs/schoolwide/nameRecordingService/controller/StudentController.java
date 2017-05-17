package hbs.schoolwide.nameRecordingService.controller;

import hbs.common.appnaccess.AuthenticationContextImpl;
import hbs.common.appnaccess.IPerson;
import hbs.common.service.configuration.ConfigurationService;
import hbs.common.service.configuration.spring.ConfigurationServiceAware;
import hbs.schoolwide.nameRecordingService.common.Constants;
import hbs.schoolwide.nameRecordingService.dto.UserDTO;
import hbs.schoolwide.nameRecordingService.vo.UserVO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.Iterator;

/**
 * Created by spillai on 3/15/2017.
 */
@RestController
@RequestMapping("student")
public class StudentController extends ConfigurationServiceAware {

    private static final Logger LOG = LoggerFactory.getLogger(StudentController.class);

    @RequestMapping(value = "/nameRecord", method = RequestMethod.GET)
    public ModelAndView recordingLandingPage(){

        IPerson person = AuthenticationContextImpl.instance().getPerson();

        String nameRecordingLandingPage = getConfigurationService().getConfiguration().getString(Constants.STUDENT_NAME_RECORDING_LANDING_PAGE);

        LOG.info("Name Recording page accessed by student: " + person.getPrimaryEmailAddress());
        return new ModelAndView("redirect:"+ nameRecordingLandingPage);
    }

    @RequestMapping(value = "/userDetail", method = RequestMethod.GET)
    public UserVO getLoggedInPersonDetail(){

        IPerson person = AuthenticationContextImpl.instance().getPerson();

        UserVO user = new UserVO(person);

        return user;
    }

    @RequestMapping(value = "/uploadForm", method = RequestMethod.POST)
    public boolean handleFileUpload(MultipartHttpServletRequest request){

        IPerson person = AuthenticationContextImpl.instance().getPerson();
        LOG.info("Handle HTML5 Recording for User: " + person.getPersonId() + ", " + person.getPrimaryEmailAddress());

        Iterator<String> iterator = request.getFileNames();

        MultipartFile mf = request.getFile(iterator.next());

        try {
            multipartToFile(mf, person.getPersonId());
        } catch (IllegalStateException ise) {
            LOG.info("Exception converting Multipart file to File: " + ise);
            return false;
        } catch (IOException ioe) {
            LOG.info("Exception converting Multipart file to File: " + ioe);
            return false;
        }
        return true;
    }

    @RequestMapping(value = { "/uploadStream" }, method = RequestMethod.POST)
    public boolean handleInputStream(final HttpServletRequest request) {

        IPerson person = AuthenticationContextImpl.instance().getPerson();
        String filePreviewLocation = ConfigurationService.getInstance().getConfiguration().getString(Constants.NAME_RECORDING_PREVIEW_LOCATION);

        LOG.info("Handle Flash Recording for User: " + person.getPersonId() + ", " + person.getPrimaryEmailAddress());

        InputStream is = null;
        FileOutputStream fs = null;
        try {
            is = request.getInputStream();

            String fileName = person.getPersonId() + ".wav";

            fs = new FileOutputStream(filePreviewLocation + fileName);
            byte[] tempbuffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(tempbuffer)) != -1) {
                fs.write(tempbuffer, 0, bytesRead);
            }
        } catch (IOException ioe) {
            LOG.info("Exception converting Inputstream to FileOutputStream: " + ioe);
            return false;
        } finally {
            try {
                is.close();
                fs.close();
            } catch (IOException ioe) {
                LOG.info("Exception trying to close Input and FileOutput Stream: " + ioe);
                return false;
            }
        }
        return true;
    }

    @RequestMapping(value = { "/recordingSubmit" }, method = RequestMethod.POST)
    public boolean handleRecordingSubmit(){
        IPerson person = AuthenticationContextImpl.instance().getPerson();

        LOG.info("Handle Recording Submit for User: " + person.getPersonId() + ", " + person.getPrimaryEmailAddress());

        String filePreviewLocation = ConfigurationService.getInstance().getConfiguration().getString(Constants.NAME_RECORDING_PREVIEW_LOCATION);
        String fileFinalUploadLocation = ConfigurationService.getInstance().getConfiguration().getString(Constants.NAME_RECORDING_UPLOAD_LOCATION);

        String fileName = person.getPersonId() + ".wav";

        File sourceFile = new File(filePreviewLocation + fileName);
        File destinationFile = new File(fileFinalUploadLocation + fileName);

        try{
            FileUtils.copyFile(sourceFile,destinationFile);
        }catch (IOException ioe){
            LOG.info("Exception trying to Copy file from preview to final upload location: " + ioe);
            return false;
        } finally {
            deleteFile(sourceFile);
        }
        return true;
    }

    @RequestMapping(value = { "/recordingCancel" }, method = RequestMethod.POST)
    public void handleRecordingCancel(){
        IPerson person = AuthenticationContextImpl.instance().getPerson();

        LOG.info("Handle Recording Cancel for User: " + person.getPersonId() + ", " + person.getPrimaryEmailAddress());

        String filePreviewLocation = ConfigurationService.getInstance().getConfiguration().getString(Constants.NAME_RECORDING_PREVIEW_LOCATION);
        String fileName = person.getPersonId() + ".wav";

        File previewFile = new File(filePreviewLocation + fileName);

        deleteFile(previewFile);
    }

    public File multipartToFile(MultipartFile multipart, Long personId) throws IllegalStateException, IOException
    {
        LOG.info("Multipart file upload process begins for person id: " + personId);

        String filePreviewLocation = ConfigurationService.getInstance().getConfiguration().getString(Constants.NAME_RECORDING_PREVIEW_LOCATION);
        String ffmpegPathCommand = ConfigurationService.getInstance().getConfiguration().getString(Constants.FFMPEG_PATH_COMMAND);

        //Parse to get file extension
        String fileExtension = multipart.getOriginalFilename().split("\\.")[1];

        String fileName = personId +".wav";
        File convFile = new File(filePreviewLocation + fileName);

        //Delete any existing preview .wav files
        if(convFile.exists()) {
            deleteFile(convFile);
        }

        //Handle iOS recording. Convert .mov file to .wav using ffmpeg
        if(fileExtension.equals("mov")){
            String fileNamemov = personId + ".mov";
            String movFilePath = filePreviewLocation + fileNamemov;
            String wavFilePath = filePreviewLocation + fileName;
            LOG.info("iOS device used." + multipart.getOriginalFilename() + " file uploaded for person: " + personId);

            File convFileMov = new File(movFilePath);
            multipart.transferTo(convFileMov);
            ProcessBuilder pb = new ProcessBuilder(ffmpegPathCommand,"-i",movFilePath,wavFilePath);
            pb.start();

            LOG.info("File conversion completed for person id: " + personId + ". Video file: " + convFileMov.getName() + " converted to Audio file: " + convFile.getName());
            if(convFile.exists()) {
                deleteFile(convFileMov);
            }
        } else {
            multipart.transferTo(convFile);
            LOG.info("File transfer process completed for person id: " + personId + ". Preview Audio file created: " + convFile.getName());
        }
        return convFile;
    }

    public void deleteFile(File fileToDelete) {

        try{
            if(fileToDelete.delete()){
                LOG.info("File: " + fileToDelete.getName() + " successfully deleted.");
            } else {
                LOG.info("File: " + fileToDelete.getName() + " delete failed.");
            }
        } catch(Exception e){
            LOG.error("Error deleting file: " + e);
        }

    }
}
