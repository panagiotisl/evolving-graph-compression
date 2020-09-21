package gr.uoa.di.networkanalysis;

import java.time.Instant;
/*
 * Use this class to define how date differences should be mapped.
 * Gives the user the freedom to define for example the difference
 * between two instances to be the number of integer hour quarters
 * between them.
 */
public interface InstantComparer {

	long instantsDifference(Instant i1, Instant i2);
}
