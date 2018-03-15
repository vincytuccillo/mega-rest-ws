/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.11
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package nz.mega.sdk;

public class MegaAccountBalance {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected MegaAccountBalance(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(MegaAccountBalance obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  protected synchronized void delete() {   
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        megaJNI.delete_MegaAccountBalance(swigCPtr);
      }
      swigCPtr = 0;
    }
}

  public double getAmount() {
    return megaJNI.MegaAccountBalance_getAmount(swigCPtr, this);
  }

  public String getCurrency() {
    return megaJNI.MegaAccountBalance_getCurrency(swigCPtr, this);
  }

  public MegaAccountBalance() {
    this(megaJNI.new_MegaAccountBalance(), true);
  }

}