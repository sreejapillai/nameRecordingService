package hbs.schoolwide.nameRecordingService.controller;

import hbs.common.service.configuration.ConfigurationService;
import hbs.common.service.configuration.spring.ConfigurationServiceAware;
import hbs.schoolwide.nameRecordingService.common.Constants;
import hbs.schoolwide.nameRecordingService.dto.StudentDTO;
import hbs.schoolwide.nameRecordingService.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by spillai on 2/23/2017.
 */
@RestController
@RequestMapping("report")
public class ReportController extends ConfigurationServiceAware {

    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView reportLandingPage(){

        String reportLandingPage = getConfigurationService().getConfiguration().getString(Constants.ADMIN_REPORT_LANDING_PAGE);
        return new ModelAndView("redirect:"+ reportLandingPage);
    }

    @RequestMapping(value = "/studentList", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    public List<StudentDTO> getStudentList()
    {
        return reportService.getStudentList();
    }

    @RequestMapping(value = "/student/note", method = RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    public StudentDTO someMethod(@RequestBody StudentDTO studentDTO) {

        return reportService.editStudentRecordingNote(studentDTO.getPersonId(),studentDTO.getRecordingNote());
    }
}
