package tourrecommendation.model;

public class TourInput {
	// database
	private VisitPoint[] visitPoints;
	private CostMoveElement[] costMoveElements;
	private CostTicketElement[] costTicketElements;
	private TimeMoveElement[] timeMoveElements;
	private TimeVisitElement[] timeVisitElements;
	private CostStayElement[] costStayElements;
	
	// input request
	private VisitPoint startPoint;
	private VisitPoint endPoint;
	private int budget;
	private String hotelCategory;
	private String tourCategory;
	private String startDate;// 2017-10-03
	private String startTime;// 09:00:00
	private String endDate;// 2017-10-05
	private String endTime;// 12:00:00
	private VisitorElement[] visitorElements;
	
}
