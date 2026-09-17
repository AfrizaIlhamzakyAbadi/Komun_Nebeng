/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto;

import java.math.BigInteger;

/**
 * An "extended" interface for classes implementing DSA-style algorithms, that provides access to
 * the group order.
 */
@android.annotation.Hide // This class is not part of the Android public SDK API
public interface DSAExt
    extends DSA
{
    /**
     * Get the order of the group that the r, s values in signatures belong to.
     */
    public BigInteger getOrder();
}
