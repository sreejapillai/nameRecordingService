package hbs.schoolwide.nameRecordingService.service;

import hbs.schoolwide.nameRecordingService.dto.StudentDTO;
import hbs.schoolwide.nameRecordingService.mapper.ReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by spillai on 2/23/2017.
 */
@Service
public class ReportService {

    @Autowired
    private ReportMapper reportMapper;

    public List<StudentDTO> getStudentList(){
        return reportMapper.getStudentList();
    }

    @Transactional
    public StudentDTO editStudentRecordingNote(Long personId, String note){
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("personId", personId);
        params.put("note", note);
        reportMapper.mergeIntoNameRecordingNote(params);

        return reportMapper.getStudentRecord(personId);
    }


}
