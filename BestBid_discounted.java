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
public class BestBid_discounted extends AbstractNegotiationParty {

	private double discountFactor = 0; // if you want to keep the discount
										// factor
	private double reservationValue = 0; // if you want to keep the reservation
											// value
	Bid maxBid = null;
	Bid opponentBid = null;
	double targetUtility = 0.0;

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

		// if you need to initialize some variables, please initialize them
		// below

	}

	private Bid getBestBid() {
		Bid bid = null;
		try {
			// bid = utilitySpace.getDomain().getRandomBid();
			if (maxBid == null) {
				maxBid = utilitySpace.getMaxUtilityBid();
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
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {

		double disc=Math.pow(discountFactor,timeline.getTime());
		if(discountFactor==1.0)
		{
			if (timeline.getTime() < 1.0) {
				Bid selfBid = getBestBid();
				return new Offer(selfBid);
			} 
			else {
				return new Accept();
			}
		}
		else
		{
			Bid selfBid = getBestBid();
			if (getUtilityWithDiscount(opponentBid)>=getUtilityWithDiscount(selfBid)) {
				//System.out.println("utility: "+getUtility(opponentBid)+", discounted utility: "+getUtilityWithDiscount(opponentBid)+", d^currentTime: "+(Math.pow(discountFactor,timeline.getCurrentTime()-2)));
				return new Accept();
			} 
			else {	
				double roundsForCurrUtility=timeline.getTotalTime()*((Math.log(getUtilityWithDiscount(opponentBid))-Math.log(getUtilityWithDiscount(selfBid)))/Math.log(discountFactor)-timeline.getTime());
				double prob=0.0;
				if ((timeline.getTotalTime() - timeline.getTime()) > 0.00000001)
					prob=1-roundsForCurrUtility/(timeline.getTotalTime() - timeline.getCurrentTime());
				else
					prob=1;
				System.out.println(roundsForCurrUtility+" "+prob);
				if (Math.random()<prob)
				{
					return new Accept();
				}
				else if(getUtilityWithDiscount(selfBid)>reservationValue*disc)
				{
					return new Offer(selfBid);
				}
				else
				{
					return new EndNegotiation();
				}
			}
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
