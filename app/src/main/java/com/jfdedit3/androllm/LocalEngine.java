package com.jfdedit3.androllm;
public final class LocalEngine {
  static { System.loadLibrary("androllm"); }
  public static native String load(String path);
  public static native String generate(String prompt,int maxTokens);
}
