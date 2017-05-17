package hbs.schoolwide.nameRecordingService.mapper;

import hbs.schoolwide.nameRecordingService.dto.StudentDTO;

import java.util.List;
import java.util.Map;

/**
 * Created by spillai on 2/23/2017.
 */
public interface ReportMapper {
    List<StudentDTO> getStudentList();
    StudentDTO getStudentRecord(Long personId);
    void mergeIntoNameRecordingNote(Map<String,Object> params);
}
