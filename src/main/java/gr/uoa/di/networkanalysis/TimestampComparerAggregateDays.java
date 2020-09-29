package gr.uoa.di.networkanalysis;

public class TimestampComparerAggregateDays implements TimestampComparer {

	@Override
	public long timestampsDifference(long t1, long t2) {
		// TODO: Check for a better way to do this. For min timestamps as well!!!
		return t2/86400-t1/86400;
	}

	@Override
	public long reverse(long previous, long difference) {
		return previous + 86400*difference;
	}

	@Override
	public long aggregateMinTimestamp(long min) {
		return (min/86400)*86400;
	}

}
