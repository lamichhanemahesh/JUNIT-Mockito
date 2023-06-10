package com.luv2code.springmvc;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.GradebookCollegeStudent;
import com.luv2code.springmvc.models.HistoryGrade;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.models.ScienceGrade;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;

@TestPropertySource("/application-test.properties")
@SpringBootTest
public class StudentAndGradeServiceTest {
	
	@Autowired
	private StudentAndGradeService studentService;
	
	@Autowired
	private StudentDao studentDao;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private MathGradesDao mathGradesDao;
	
	@Autowired
	private ScienceGradesDao scienceGradesDao;
	
	@Autowired
	private HistoryGrade historyGrade;
	
	@Autowired
	private HistoryGradesDao historyGradesDao;
	
	@Value("${sql.script.create.student}")
	private String sqlAddStudent;
	
	@Value("${sql.script.delete.student}")
	private String sqlDeleteStudent;
	
	
	@BeforeEach
	public void setupDatabase() {
		jdbcTemplate.execute(sqlAddStudent);
		jdbcTemplate.execute("insert into math_grade(id,student_id,grade) values(1,1,100.00)");
		jdbcTemplate.execute("insert into science_grade(id,student_id,grade) values(1,1,100.00)");
		jdbcTemplate.execute("insert into history_grade(id,student_id,grade) values(1,1,100.00)");
	}
	
	@AfterEach
	public void setupAfterTransaction() {
		jdbcTemplate.execute(sqlDeleteStudent);
		jdbcTemplate.execute("delete from history_grade");
		jdbcTemplate.execute("delete from science_grade");
		jdbcTemplate.execute("delete from math_grade");
	}

	@Test
	public void createStudentService() {
		
		studentService.createStudent("Chad", "Darby", "chad.darby@luv2code_school.com");
		CollegeStudent student = studentDao.findByEmailAddress("chad.darby@luv2code_school.com");
		assertEquals("chad.darby@luv2code_school.com",student.getEmailAddress(),"find by email");
	}
	
	@Test
	public void isStudentNullCheck() {
		assertTrue(studentService.checkIfStudentNull(1));
		assertFalse(studentService.checkIfStudentNull(0));
	}
	
	@Test
	public void deleteStudentService() {
		Optional<CollegeStudent> deletedCollegeStudent = studentDao.findById(1);
		Optional<MathGrade> deletedMathGrade = mathGradesDao.findById(1);
		Optional<ScienceGrade> deletedScienceGrade= scienceGradesDao.findById(1);
		Optional<HistoryGrade> deletedHistoryGrade = historyGradesDao.findById(1);
		
		assertTrue(deletedCollegeStudent.isPresent(),"Return True");
		assertTrue(deletedMathGrade.isPresent(),"Return True");
		assertTrue(deletedScienceGrade.isPresent(),"Return True");
		assertTrue(deletedHistoryGrade.isPresent(),"Return True");
		
		studentService.deleteStudent(1);
		
		
		deletedCollegeStudent = studentDao.findById(1);
		deletedMathGrade = mathGradesDao.findById(1);
		deletedScienceGrade = scienceGradesDao.findById(1);
		deletedHistoryGrade = historyGradesDao.findById(1);
		
		assertFalse(deletedCollegeStudent.isPresent(),"Return False");
		assertFalse(deletedMathGrade.isPresent(),"Return False");
		assertFalse(deletedScienceGrade.isPresent(),"Return False");
		assertFalse(deletedHistoryGrade.isPresent(),"Return False");
		
	}
	
	@Sql("/insertData.sql")
	@Test
	public void getGradebookService() {
		
		Iterable<CollegeStudent> iterableCollegeStudents = studentService.getGradebook();
		
		List<CollegeStudent> collegeStudents = new ArrayList<>();
		
		for(CollegeStudent collegeStudent: iterableCollegeStudents) {
			collegeStudents.add(collegeStudent);
		}
		
		assertEquals(5,collegeStudents.size());
		
	}
	

	@Test
	public void createGradeService() {
		
		// Create the grade
		assertTrue(studentService.createGrade(80.50,1,"math"));
		assertTrue(studentService.createGrade(80.50,1,"science"));
		assertTrue(studentService.createGrade(80.50,1,"history"));
		
		// Get all grades with studentId
		Iterable<MathGrade> mathGrades = mathGradesDao.findGradeByStudentId(1);
		Iterable<ScienceGrade> scienceGrades = scienceGradesDao.findGradeByStudentId(1);
		Iterable<HistoryGrade> historyGrades = historyGradesDao.findGradeByStudentId(1);
		
		// Verify there is grades
		assertTrue(((Collection<MathGrade>) mathGrades).size() == 2,"Student has math grades");
		assertTrue(((Collection<ScienceGrade>) scienceGrades).size() == 2,"Student has science grades");
		assertTrue(((Collection<HistoryGrade>) historyGrades).size() == 2,"Student has history grades");
		
	}
	
	@Test
	public void createGradeServiceReturnFalse() {
		// Create the grade
		assertFalse(studentService.createGrade(105.50,1,"math"));
		assertFalse(studentService.createGrade(-5,1,"science"));
		assertFalse(studentService.createGrade(80.50,2,"history"));
		assertFalse(studentService.createGrade(80.50,2,"literature"));
	}
	
	@Test
	public void deleteGradeService() {
		assertEquals(1,studentService.deleteGrade(1,"math"),"Returns student id after delete");
		assertEquals(1,studentService.deleteGrade(1,"science"),"Returns student id after delete");
		assertEquals(1,studentService.deleteGrade(1,"history"),"Returns student id after delete");
	}
	
	@Test
	public void deleteGradeServiceReturnStudentIdOfZero() {
		assertEquals(0,studentService.deleteGrade(0, "science"));
		assertEquals(0,studentService.deleteGrade(1, "literature"));
	}
	
	@Test
	public void studentInformation() {
		GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformations(1);
		assertNotNull(gradebookCollegeStudent);
		assertEquals(1,gradebookCollegeStudent.getId());
		assertEquals("Eric",gradebookCollegeStudent.getFirstname());
		assertEquals("Roby",gradebookCollegeStudent.getLastname());
		assertEquals("eric.roby@luv2code_school.com",gradebookCollegeStudent.getEmailAddress());
		
		assertTrue(gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size() == 1);
		assertTrue(gradebookCollegeStudent.getStudentGrades().getScienceGradeResults().size() == 1);
		assertTrue(gradebookCollegeStudent.getStudentGrades().getHistoryGradeResults().size() == 1);
	}
	
	@Test
	public void studentInformationServiceReturnNull() {
		GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformations(0);
		assertNull(gradebookCollegeStudent);
	}
	
	
	
	
	
	

	
}
