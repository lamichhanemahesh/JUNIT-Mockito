package com.luv2code.springmvc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.Grade;
import com.luv2code.springmvc.models.GradebookCollegeStudent;
import com.luv2code.springmvc.models.HistoryGrade;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.models.ScienceGrade;
import com.luv2code.springmvc.models.StudentGrades;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;

@Service
@Transactional
public class StudentAndGradeService {

	@Autowired
	StudentDao studentDao;

	@Autowired
	@Qualifier("mathGrades")
	private MathGrade mathGrade;

	@Autowired
	@Qualifier("scienceGrades")
	private ScienceGrade scienceGrade;

	@Autowired
	@Qualifier("historyGrades")
	private HistoryGrade historyGrade;

	@Autowired
	private MathGradesDao mathGradesDao;

	@Autowired
	private ScienceGradesDao scienceGradesDao;

	@Autowired
	private HistoryGradesDao historyGradesDao;

	@Autowired
	private StudentGrades studentGrades;

	public void createStudent(String firstname, String lastname, String emailAddress) {
		CollegeStudent student = new CollegeStudent(firstname, lastname, emailAddress);
		student.setId(0);
		studentDao.save(student);
	}

	public boolean checkIfStudentNull(int id) {
		Optional<CollegeStudent> student = studentDao.findById(id);
		if (student.isPresent()) {
			return true;
		}
		return false;

	}

	public void deleteStudent(int id) {

		if (checkIfStudentNull(id)) {
			studentDao.deleteById(id);
			mathGradesDao.deleteById(id);
			scienceGradesDao.deleteById(id);
			historyGradesDao.deleteById(id);
		}
	}

	public Iterable<CollegeStudent> getGradebook() {

		Iterable<CollegeStudent> collegeStudents = studentDao.findAll();
		return collegeStudents;

	}

	public boolean createGrade(double grade, int studentId, String gradeType) {

		if (!checkIfStudentNull(studentId)) {
			return false;
		}
		if (grade >= 0 && grade <= 100) {
			if (gradeType.equals("math")) {
				mathGrade.setId(0);
				mathGrade.setGrade(grade);
				mathGrade.setStudentId(studentId);
				mathGradesDao.save(mathGrade);
				return true;
			} else if (gradeType.equals("science")) {
				scienceGrade.setId(0);
				scienceGrade.setGrade(grade);
				scienceGrade.setStudentId(studentId);
				scienceGradesDao.save(scienceGrade);
				return true;
			} else if (gradeType.equals("history")) {
				historyGrade.setId(0);
				historyGrade.setGrade(grade);
				historyGrade.setStudentId(studentId);
				historyGradesDao.save(historyGrade);
				return true;
			}
		}
		return false;
	}

	public Integer deleteGrade(int id, String gradeType) {
		int studentId = 0;
		if (gradeType.equals("math")) {
			Optional<MathGrade> grade = mathGradesDao.findById(id);
			if (!grade.isPresent()) {
				return studentId;
			}
			studentId = grade.get().getStudentId();
			mathGradesDao.deleteById(id);
		} else if (gradeType.equals("science")) {
			Optional<ScienceGrade> grade = scienceGradesDao.findById(id);
			if (!grade.isPresent()) {
				return studentId;
			}
			studentId = grade.get().getStudentId();
			scienceGradesDao.deleteById(id);
		} else if (gradeType.equals("history")) {
			Optional<HistoryGrade> grade = historyGradesDao.findById(id);
			if (!grade.isPresent()) {
				return studentId;
			}
			studentId = grade.get().getStudentId();
			historyGradesDao.deleteById(id);
		}

		return studentId;
	}

	public com.luv2code.springmvc.models.GradebookCollegeStudent studentInformations(int id) {

		if (!checkIfStudentNull(id)) {
			return null;
		}

		Optional<CollegeStudent> student = studentDao.findById(id);
		Iterable<MathGrade> mathGrades = mathGradesDao.findGradeByStudentId(id);
		Iterable<ScienceGrade> scienceGrades = scienceGradesDao.findGradeByStudentId(id);
		Iterable<HistoryGrade> historyGrades = historyGradesDao.findGradeByStudentId(id);

		List<Grade> mathGradesList = new ArrayList<>();
		mathGrades.forEach(mathGradesList::add);

		List<Grade> scienceGradesList = new ArrayList<>();
		scienceGrades.forEach(scienceGradesList::add);

		List<Grade> historyGradesList = new ArrayList<>();
		historyGrades.forEach(historyGradesList::add);

		studentGrades.setMathGradeResults(mathGradesList);
		studentGrades.setScienceGradeResults(scienceGradesList);
		studentGrades.setHistoryGradeResults(historyGradesList);

		GradebookCollegeStudent gradebookCollegeStudent = new GradebookCollegeStudent(student.get().getId(),
				student.get().getFirstname(), student.get().getLastname(), student.get().getEmailAddress(),
				studentGrades);

		return gradebookCollegeStudent;
	}

	public void configureStudentInformationModel(int id, Model m) {
		GradebookCollegeStudent studentEntity = studentInformations(id);
		m.addAttribute("student", studentEntity);

		if (studentEntity.getStudentGrades().getMathGradeResults().size() > 0) {
			m.addAttribute("mathAverage", studentEntity.getStudentGrades()
					.findGradePointAverage(studentEntity.getStudentGrades().getMathGradeResults()));
		} else {
			m.addAttribute("mathAverage", "N/A");
		}

		if (studentEntity.getStudentGrades().getScienceGradeResults().size() > 0) {
			m.addAttribute("scienceAverage", studentEntity.getStudentGrades()
					.findGradePointAverage(studentEntity.getStudentGrades().getScienceGradeResults()));
		} else {
			m.addAttribute("scienceAverage", "N/A");
		}

		if (studentEntity.getStudentGrades().getHistoryGradeResults().size() > 0) {
			m.addAttribute("historyAverage", studentEntity.getStudentGrades()
					.findGradePointAverage(studentEntity.getStudentGrades().getHistoryGradeResults()));
		} else {
			m.addAttribute("historyAverage", "N/A");
		}
	}
}
