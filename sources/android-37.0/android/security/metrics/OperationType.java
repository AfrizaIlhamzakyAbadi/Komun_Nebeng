/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: out/host/linux-x86/bin/aidl --lang=java -Weverything -Wno-missing-permission-annotation -t --min_sdk_version platform_apis -pout/soong/.intermediates/system/hardware/interfaces/keystore2/aidl/android.system.keystore2_interface/6/preprocessed.aidl --ninja -d out/soong/.intermediates/system/security/keystore2/aidl/android.security.metrics-java-source/gen/android/security/metrics/OperationType.java.d -o out/soong/.intermediates/system/security/keystore2/aidl/android.security.metrics-java-source/gen -Nsystem/security/keystore2/aidl system/security/keystore2/aidl/android/security/metrics/OperationType.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.security.metrics;
/**
 * OperationType enum as defined in Keystore2OperationLatency of
 * frameworks/proto_logging/stats/atoms/keystore/keystore_extension_atoms.proto.
 * @hide
 */
public @interface OperationType {
  /** Unspecified operation. */
  public static final int UNSPECIFIED = 0;
  /** Key generation. */
  public static final int GENERATE_KEY = 1;
  /** Key import. */
  public static final int IMPORT_KEY = 2;
  /** Wrapped key import. */
  public static final int IMPORT_WRAPPED_KEY = 3;
  /** Operation creation (e.g. begin() call). */
  public static final int CREATE_OPERATION = 4;
  /**
   * All update() and finish() calls that are part of a single operation.
   * This represents the cumulative latency of all data processing for one operation.
   */
  public static final int ENTIRE_OPERATION = 5;
}
