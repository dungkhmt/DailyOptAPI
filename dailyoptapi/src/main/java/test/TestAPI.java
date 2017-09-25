package test;

import javax.servlet.http.HttpServletRequest;

//import org.apache.log4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestAPI {
	@RequestMapping(value = "/basic", method = RequestMethod.POST)
	public TestSolution getFields(HttpServletRequest request, @RequestBody TestInput input) {
		int c = input.getA() + input.getB();
		return new TestSolution(c);
	}

}
