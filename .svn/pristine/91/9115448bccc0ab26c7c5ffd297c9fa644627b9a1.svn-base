package agents;

/**
 * Move types and functionality for it.
 * 
 * @author W.Pasman 2sep14
 *
 */
enum MoveType {
	CONCESSION, UNFORTUNATE, FORTUNATE, SELFISH, SILENT, NICE;

	/**
	 * Get a move type associated with deltaSelf and deltaOther utility
	 * differences
	 * 
	 * @param deltaSelf
	 *            the change of the bid's utility in your own space the
	 *            difference ownUtility(bid) - ownutility(previous bid)
	 * @param deltaOther
	 *            the change of the bid's utility in the estimated other's
	 *            space. the difference otherUtility(bid) -
	 *            otherutility(previous bid)
	 * @return the MoveType for the given set of delta's
	 */
	static MoveType getMoveType(double deltaSelf, double deltaOther) {
		if (deltaOther == 0 && deltaSelf == 0)
			return SILENT;
		if (deltaOther == 0 && deltaSelf > 0)
			return NICE;
		if (deltaOther <= 0 && deltaSelf < 0)
			return UNFORTUNATE;
		if (deltaOther < 0 && deltaSelf >= 0)
			return CONCESSION;
		if (deltaOther > 0 && deltaSelf > 0)
			return FORTUNATE;
		if (deltaOther > 0 && deltaSelf <= 0)
			return SELFISH;

		// It should not be possible to get here. All case are mathematically
		// covered
		throw new IllegalStateException("Unexpected case " + deltaSelf + ","
				+ deltaOther);
	}
};