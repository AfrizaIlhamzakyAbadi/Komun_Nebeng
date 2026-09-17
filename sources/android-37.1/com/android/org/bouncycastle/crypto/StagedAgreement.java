/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto;

import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;

@android.annotation.Hide // This class is not part of the Android public SDK API
public interface StagedAgreement
    extends BasicAgreement
{
    AsymmetricKeyParameter calculateStage(CipherParameters pubKey);
}
