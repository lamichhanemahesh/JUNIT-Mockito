package com.luv2code.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.luv2code.component.models.CollegeStudent;
import com.luv2code.component.models.StudentGrades;

@SpringBootTest
public class ReflectionTestUtilsTest {

	@Autowired
	ApplicationContext context;
	
	@Autowired
	CollegeStudent studentOne;
	
	@Autowired
	StudentGrades studentGrades;
	
	
//	@MockBean
//	private ApplicationDao applicationDao;
//	
//	@Autowired
//	private ApplicationService applicationService;
	
	@BeforeEach
	public void beforeEach() {
		studentOne.setFirstname("Eric");
		studentOne.setLastname("Roby");
		studentOne.setEmailAddress("eric.roby@luv2code.com");
		studentOne.setStudentGrades(studentGrades);
		
		ReflectionTestUtils.setField(studentOne, "id", 1);
		ReflectionTestUtils.setField(studentOne, "studentGrades", new StudentGrades(List.of(100.0,85.0,76.50,91.75)));
	}
	
	@Test
	public void getPrivateField() {
		assertEquals(1,ReflectionTestUtils.getField(studentOne, "id"));
	}
	
	@Test
	public void invokePrivateMethod() {
		assertEquals("Eric 1",ReflectionTestUtils.invokeMethod(studentOne, "getFirstNameAndId"),"Fail private method not call");
	}
	
	
	
	
	
	
	
	
	
}
