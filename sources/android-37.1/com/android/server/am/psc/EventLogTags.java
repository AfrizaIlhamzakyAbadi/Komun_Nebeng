/* This file is auto-generated.  DO NOT MODIFY.
 * Source file: frameworks/base/services/core/java/com/android/server/am/psc/EventLogTags.logtags
 */

package com.android.server.am.psc;

@android.annotation.Hide
public class EventLogTags {
  private EventLogTags() { }  // don't instantiate

  /** 30054 am_uid_active (UID|1|5) */
  public static final int AM_UID_ACTIVE = 30054;

  /** 30111 am_uid_state_changed (UID|1|5),(Seq|1|5),(UidState|1|5),(OldUidState|1|5),(Capability|1|5),(OldCapability|1|5),(Flags|1|5),(reason|3) */
  public static final int AM_UID_STATE_CHANGED = 30111;

  /** 30112 am_proc_state_changed (UID|1|5),(PID|1|5),(Seq|1|5),(ProcState|1|5),(OldProcState|1|5),(OomAdj|1|5),(OldOomAdj|1|5),(reason|3) */
  public static final int AM_PROC_STATE_CHANGED = 30112;

  /** 30113 am_oom_adj_misc (Event|1|5),(UID|1|5),(PID|1|5),(Seq|1|5),(Arg1|1|5),(Arg2|1|5),(reason|3) */
  public static final int AM_OOM_ADJ_MISC = 30113;

  public static void writeAmUidActive(int uid) {
    android.util.EventLog.writeEvent(AM_UID_ACTIVE, uid);
  }

  public static void writeAmUidStateChanged(int uid, int seq, int uidstate, int olduidstate, int capability, int oldcapability, int flags, String reason) {
    android.util.EventLog.writeEvent(AM_UID_STATE_CHANGED, uid, seq, uidstate, olduidstate, capability, oldcapability, flags, reason);
  }

  public static void writeAmProcStateChanged(int uid, int pid, int seq, int procstate, int oldprocstate, int oomadj, int oldoomadj, String reason) {
    android.util.EventLog.writeEvent(AM_PROC_STATE_CHANGED, uid, pid, seq, procstate, oldprocstate, oomadj, oldoomadj, reason);
  }

  public static void writeAmOomAdjMisc(int event, int uid, int pid, int seq, int arg1, int arg2, String reason) {
    android.util.EventLog.writeEvent(AM_OOM_ADJ_MISC, event, uid, pid, seq, arg1, arg2, reason);
  }
}
