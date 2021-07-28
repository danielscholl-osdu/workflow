package org.opengroup.osdu.workflow.provider.azure.utils;

public class ThreadLocalUtils {
  private static final ThreadLocal<Boolean> isSystemDag =
      ThreadLocal.withInitial(() -> false);
  public static Boolean getSystemDagFlag() {
    return isSystemDag.get();
  }
  public static void setSystemDagFlag(Boolean isSystemDagInput) {
    isSystemDag.set(isSystemDagInput);
  }
}
