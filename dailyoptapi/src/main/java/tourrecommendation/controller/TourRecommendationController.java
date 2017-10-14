package tourrecommendation.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tourrecommendation.model.TourInput;
import tourrecommendation.model.TourSolution;
import tourrecommendation.service.TourRecommender;

@RestController
public class TourRecommendationController {
	@RequestMapping(value = "/compute-tour", method = RequestMethod.POST)
	public TourSolution getFields(HttpServletRequest request, @RequestBody TourInput input) {
		// TODO
		TourRecommender solver = new TourRecommender();
		
		return solver.search(input);
	}
}
