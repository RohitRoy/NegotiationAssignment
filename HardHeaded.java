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
public class HardHeaded extends AbstractNegotiationParty {

	private double discountFactor = 0; // if you want to keep the discount
										// factor
	private double reservationValue = 0; // if you want to keep the reservation
											// value
	Bid maxBid = null;
	Bid opponentBid = null;
	double targetUtility = 0.0;
	double totalTime = 0;
    double epsilon = 0.00000001;

	@Override
	public void init(AbstractUtilitySpace utilSpace, Deadline dl,
			TimeLineInfo tl, long randomSeed, AgentID agentId) {

		System.out.println("Deadline is " + dl);
		System.out.println("Deadline is " + dl.getType());
		System.out.println("Deadline is " + dl.getValue()*0.85);


		super.init(utilSpace, dl, tl, randomSeed, agentId);

		discountFactor = utilSpace.getDiscountFactor(); // read discount factor
		System.out.println("Discount Factor is " + discountFactor);
		reservationValue = utilSpace.getReservationValueUndiscounted(); // read
																		// reservation
																		// value
		System.out.println("Reservation Value is " + reservationValue);

		// if you need to initialize some variables, please initialize them
		// below

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

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */

	private double P(double t) {
		double k=0.05;
		double e=0.12;
		double F=k+(1-k)*Math.pow(Math.min(t,1.0),1.0/e);
		double minUtility = Math.max(0.58,reservationValue);//reservationValue;
		return minUtility + (1.0-F)*(1.0-minUtility);
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		// with 50% chance, counter offer
		// if we are the first party, also offer.

		//if (opponentBid != null && getUtility(opponentBid) > targetUtility)
		//	targetUtility = getUtility(opponentBid);
		double disc = Math.pow(discountFactor,timeline.getTime());
        double timeNow = timeline.getCurrentTime();
        double currUtility=P(timeline.getTime());
        System.out.println("P(t) = "+currUtility);
        Bid selfBid = getRandomBid(currUtility);
        double oppBidDiscUtil = getUtilityWithDiscount(opponentBid); // utility with discount factor for self w.r.t. opponentBid
        double selfBidDiscUtil = getUtilityWithDiscount(selfBid); // utility with discount factor for self w.r.t. selfBid
        System.out.println("Self = "+selfBidDiscUtil);
        System.out.println("Opp = "+oppBidDiscUtil);
        if(oppBidDiscUtil>selfBidDiscUtil)
        	return new Accept();
        else if(selfBidDiscUtil<reservationValue)
        	return new EndNegotiation();
        else
        {
        	System.out.println("Ho");
        	return new Offer(selfBid);
        }
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
		// Here you hear other parties' messages
	}

	@Override
	public String getDescription() {
		return "example party group N";
	}

}
