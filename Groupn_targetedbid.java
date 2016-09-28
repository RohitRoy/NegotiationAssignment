package multipartyexample;

import java.util.List;

import negotiator.AgentID;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.Deadline;
import negotiator.session.TimeLineInfo;
import negotiator.Bid;

/**
 * This is your negotiation party.
 */
public class Groupn_targetedbid extends AbstractNegotiationParty {

	private double discountFactor = 0; // if you want to keep the discount
										// factor
	private double reservationValue = 0; // if you want to keep the reservation
											// value
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

	private Bid getRandomBid(double target) {
		Bid bid = null;
		try {
			int loops = 0;
			do {
				bid = utilitySpace.getDomain().getRandomBid();
				loops++;
			} while (loops < 100000 && utilitySpace.getUtility(bid) < target);
			if (bid == null) {
				System.out.println("Crossed 10,000 loops");
				bid=utilitySpace.getDomain().getRandomBid();
			/*	if (maxBid == null) {
					// this is a computationally expensive operation, therefore
					// cache result
					maxBid = utilitySpace.getMaxUtilityBid();
				}
				bid = maxBid;
			*/}
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
		// with 50% chance, counter offer
		// if we are the first party, also offer.
		if (opponentBid != null && getUtility(opponentBid) > targetUtility)
			targetUtility = getUtility(opponentBid);
		System.out.println("Target Utility (post op): " + targetUtility);

		if (!validActions.contains(Accept.class) || targetUtility < 0.8) {
			Bid selfBid = getRandomBid(targetUtility);
			// if (selfBid != null && getUtility(selfBid) > targetUtility)
			// 	targetUtility = getUtility(selfBid);
			// System.out.println("Target Utility (pre bid): " + targetUtility);
			return new Offer(selfBid);
		} else {
			return new Accept();
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
