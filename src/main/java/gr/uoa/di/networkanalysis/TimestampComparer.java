package gr.uoa.di.networkanalysis;

/*
 * Use this class to define how date differences should be mapped.
 * Gives the user the freedom to define for example the difference
 * between two instances to be the number of integer hour quarters
 * between them.
 */
public interface TimestampComparer {

	long timestampsDifference(long t1, long t2);
	long reverse(long previous, long difference);
}
