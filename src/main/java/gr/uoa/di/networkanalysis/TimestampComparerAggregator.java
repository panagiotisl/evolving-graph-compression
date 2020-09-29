package gr.uoa.di.networkanalysis;

public class TimestampComparerAggregator implements TimestampComparer {

	private final int factor;
	
	public TimestampComparerAggregator(int factor) {
		this.factor = factor;
	}
	
	@Override
	public long timestampsDifference(long t1, long t2) {
		// TODO: Check for a better way to do this. For min timestamps as well!!!
		return t2/factor-t1/factor;
	}

	@Override
	public long reverse(long previous, long difference) {
		return previous + factor*difference;
	}

	@Override
	public long aggregateMinTimestamp(long min) {
		return (min/factor)*factor;
	}

}
