package gr.uoa.di.networkanalysis;

public class TimestampComparerAggregateSeconds implements TimestampComparer {

	@Override
	public long timestampsDifference(long t1, long t2) {
		//return EpochUtils.getDifference(t1, t2, 1, ChronoUnit.DAYS);
		return t2-t1;
	}

	@Override
	public long reverse(long previous, long difference) {
		//return previous + EpochUtils.getTimestampFromAggregation(difference, 1, ChronoUnit.DAYS);
		return previous + difference;
	}

	@Override
	public long aggregateMinTimestamp(long min) {
		return min;
	}

}
