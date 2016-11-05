package multipartyexample;

import java.util.List;

import negotiator.AgentID;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.actions.EndNegotiation;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.Deadline;
import negotiator.session.TimeLineInfo;
import negotiator.Bid;
import java.lang.Math;

/**
 * This is your negotiation party.
 */
public class DetectorAgent extends AbstractNegotiationParty {

	private double discountFactor = 0; // if you want to keep the discount
										// factor
	private double reservationValue = 0; // if you want to keep the reservation
											// value

	Bid maxBid = null;
	Bid opponentBid = null;
	double targetUtility = 1.0;
    double totalTime = 0;
    double epsilon = 0.00000001;

    int issNum = 0;
    java.util.ArrayList<negotiator.issue.Issue> issues = null;
    java.util.Hashtable<negotiator.issue.IssueDiscrete, java.util.List<negotiator.issue.ValueDiscrete>> discIssues = null;

    java.util.Hashtable<AgentID, Double[]> issueWts = new java.util.Hashtable<AgentID, Double[]>();
    java.util.Hashtable<AgentID, java.util.Hashtable<Integer, Double[]>> valueWts = new java.util.Hashtable<AgentID, java.util.Hashtable<Integer, Double[]>>();

    java.util.Hashtable<AgentID, java.util.ArrayList<Bid>> agentHistory = new java.util.Hashtable<AgentID, java.util.ArrayList<Bid> >();

    public static negotiator.issue.ISSUETYPE discrete = negotiator.issue.ISSUETYPE.valueOf("DISCRETE");

    int historyLen = 3;

    double[] history = {0.0, 0.0, 0.0};

	@Override
	public void init(AbstractUtilitySpace utilSpace, Deadline dl,
			TimeLineInfo tl, long randomSeed, AgentID agentId) {

		super.init(utilSpace, dl, tl, randomSeed, agentId);

		discountFactor = utilSpace.getDiscountFactor(); // read discount factor
		System.out.println("Discount Factor is " + discountFactor);
		reservationValue = utilSpace.getReservationValueUndiscounted(); // read
																		// reservation
																		// value
		System.out.println("Reservation Value is " + reservationValue);
        totalTime = timeline.getTotalTime();

		// if you need to initialize some variables, please initialize them
		// below
		issues = utilSpace.getDomain().getIssues();
		issNum = issues.size();
		for (int i=0; i != issNum; i++) {
			if (issues.get(i).getType() == discrete){
				negotiator.issue.IssueDiscrete discIssue = (negotiator.issue.IssueDiscrete) issues.get(i);
				java.util.List<negotiator.issue.ValueDiscrete> allValues = discIssue.getValues();
				discIssues.put(discIssue, allValues);
			}
		}
		
		System.out.println("IssNum " +  issNum);
		System.out.println("Issues are " + issues);
		System.out.println("Issue0 is " + issues.get(0));
		System.out.println("Issue0 is " + issues.get(0).getType());
		// if 
		System.out.println("Issue0 has " + ((negotiator.issue.IssueDiscrete) issues.get(0)).getValues());
		// java.
		System.out.println("Issue0 is " + issues.get(0).getNumber());

	}

	private Bid getBestBid() {
		Bid bid = null;
		try {
			// bid = utilitySpace.getDomain().getRandomBid();
			if (maxBid == null) {
				maxBid = utilitySpace.getMaxUtilityBid();
				System.out.println(maxBid);
			}
			bid = maxBid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bid;
	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */

	private void updateHistory (double newUtil) {
		for (int i=1; i != historyLen; i++) {
			history[i-1] = history[i];
		};
		history[historyLen-1] = newUtil;
		return;
	}

	private double getTatRatio () {
		if (timeline.getCurrentTime() <= historyLen) {
			return 1.0;
		}
		return history[historyLen-2] / history[historyLen-1];
	}

	private Bid getRandomBid(double target) {
		Bid bid = null;
		//try {
			int loops = 0;
			do {
				bid = utilitySpace.getDomain().getRandomBid();
				loops++;
			} while (loops < 100000 && utilitySpace.getUtility(bid) < target);
			if (bid == null) { 
				bid=utilitySpace.getDomain().getRandomBid();
			}
		return bid;
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {

		double disc = Math.pow(discountFactor,timeline.getTime());
        double timeNow = timeline.getTime();
        if (disc <= reservationValue) {
        	return new EndNegotiation();
        }

        double oppBidDiscUtil = getUtilityWithDiscount(opponentBid); // utility with discount factor for self w.r.t. opponentBid

        if (oppBidDiscUtil > targetUtility*disc) {
        	return new Accept();
        };

        updateHistory(oppBidDiscUtil);
        double tatRatio = getTatRatio();
        targetUtility = targetUtility * tatRatio;
        
        Bid selfBid = getRandomBid(targetUtility);
        java.util.ArrayList<negotiator.issue.Issue> iss = selfBid.getIssues();
        //java.util.Iterator itr=iss.iterator();
        //System.out.println(itr.next().getClass());
        negotiator.issue.Issue iss0 = iss.get(0);
        // System.out.println(iss0.get(0));
        System.out.println("Issue0 Number is : " + iss0.getNumber());
        int iss0num = iss0.getNumber();
        if(selfBid!=null)
        	System.out.println("Self Bid " + selfBid.getValue(iss0num));
        double selfBidDiscUtil = getUtilityWithDiscount(selfBid); // utility with discount factor for self w.r.t. selfBid

        if (timeline.getCurrentTime() < totalTime) {
			return new Offer(selfBid);
		}
		else {
			return new Accept();
		}

		// 		double roundsForCurrUtility = totalTime * (Math.log(oppBidDiscUtil)-Math.log(selfBidDiscUtil)) / Math.log(discountFactor);
		// 		double prob = 0.0;
		// 		if ((totalTime - timeNow)/totalTime > epsilon)
		// 			prob = 1 - roundsForCurrUtility / (totalTime - timeNow);
		// 		else
		// 			prob = 1;
		// 		System.out.println(roundsForCurrUtility+" "+prob);

		// 		if (Math.random() <= prob*prob)
		// 		{
		// 			return new Accept();
		// 		}
		// 		else if (selfBidDiscUtil > reservationValue)
		// 		{
		// 			return new Offer(selfBid);
		// 		}
		// 		else
		// 		{
		// 			return new EndNegotiation();
		// 		}
		// 	}
		// }
	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action. Can be null.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		opponentBid = Action.getBidFromAction(action);
		System.out.println("sender is " + sender);
		System.out.println("bid is " + opponentBid);
		if (sender == null) {
			return;
		}

		if (!issueWts.keySet().contains(sender)) {
			Double[] senderIssueWts = new Double[issNum];
			java.util.Hashtable<Integer, Double[]> senderValueWts = new java.util.Hashtable<Integer, Double[]>();
			double initWt = 1.0 / (double) issNum;
			for (int i=0; i != issNum ; i++){
				senderIssueWts[i] = initWt;
				if (issues.get(i).getType() == discrete){
					negotiator.issue.IssueDiscrete discIssue = (negotiator.issue.IssueDiscrete) issues.get(i);
					int numValues = discIssue.getNumberOfValues();
					double initValWt = 1.0 / (double) numValues;
					Double[] discValWts = new Double[numValues];
					for (int j=0; j != numValues; j++){
						discValWts[j] = initValWt;
						}
					senderValueWts.put((Integer)i, discValWts);
					
					}
				}
			issueWts.put(sender, senderIssueWts);
			valueWts.put(sender, senderValueWts);
			}
		// Here you hear other parties' messages
		senderHistory = agentHistory.get(sender);
		senderHistory.add(opponentBid);

		for (i = 0; i != issNum; i++){
			if (issue[i].getType() == discrete) {
				issueNum = issue[i].getNumber();
				issueVal = opponentBid.getValue(issueNum);
				
			}
		}
	}

	@Override
	public String getDescription() {
		return "example party group N";
	}

}
