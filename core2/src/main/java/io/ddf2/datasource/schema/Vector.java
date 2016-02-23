package io.ddf2.datasource.schema;

/**
 *
 */

import io.ddf2.IDDF;
import io.ddf2.DDFException;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;


import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;

/**
 * A one-dimensional array of values of the same type, e.g., Integer or Double or String.
 * <p/>
 * We implement a Vector as simply a reference to a column in a DDF. The DDF may have a single column, or multiple
 * columns.
 * <p/>
 * The column is referenced by name.
 * <p/>
 * TODO: Vector operations
 */
public class Vector<T> implements Serializable {

  /**
   * TODO: test this
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  protected Class<T> getParameterizedType() {
    Class<T> clazz = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
    return clazz;
  }


  /**
   * Instantiate a new Vector based on an existing DDF, given a column name. The column name is not verified for
   * correctness; any errors would only show up on actual usage.
   *
   * @param theDDF
   * @param theColumnName
   */
  public Vector(IDDF theDDF, String theColumnName) {
    this.initialize(theDDF, theColumnName);
  }

  /**
   * Instantiate a new Vector with the given T array. Uses the default engine.
   *
   * @param data
   * @param theColumnName
   * @throws DDFException
   */
  public Vector(String theColumnName, T[] data) throws DDFException {
    this.initialize(theColumnName, data, null);
  }

  /**
   * Instantiate a new Vector with the given T array. Uses the default engine.
   *
   * @param data
   * @param theColumnName
   * @param engineName
   * @throws DDFException
   */
  public Vector(String theColumnName, T[] data, String engineName) throws DDFException {
    this.initialize(theColumnName, data, engineName);
  }

  private void initialize(String name, T[] data, String engineName) throws DDFException {
    if (data == null || data.length == 0) throw new DDFException("Cannot initialize a null or zero-length Vector");

    // TODO
    /*
    DDF newDDF = DDFManager.get(DDFManager.EngineType.fromString(engineName)) //
        .newDDF(null, (Object) data, new Class[] { Array.class, this.getParameterizedType() }, null , name, //
            new Schema(name, String.format("%s %s", name, this.getParameterizedType().getSimpleName())));

    this.initialize(newDDF, name);
    */
  }

  private void initialize(IDDF theDDF, String name) {
    this.setDDF(theDDF);
    this.setDDFColumnName(name);
  }


  /**
   * The DDF that contains this vector
   */

  private transient IDDF mDDF;

  /**
   * The name of the DDF column we are pointing to
   */
  private String mDDFColumnName;


  /**
   * @return the mDDF
   */
  public IDDF getDDF() {
    return mDDF;
  }

  /**
   * @param mDDF the mDDF to set
   */
  public void setDDF(IDDF mDDF) {
    this.mDDF = mDDF;
  }

  /**
   * @return the mDDFColumnName
   */
  public String getDDFColumnName() {
    return mDDFColumnName;
  }

  /**
   * @param mDDFColumnName the mDDFColumnName to set
   */
  public void setDDFColumnName(String mDDFColumnName) {
    this.mDDFColumnName = mDDFColumnName;
  }

  // @SuppressWarnings("unchecked")
  // public Iterator<T> iterator() {
  // return (Iterator<T>) this.getDDF().getElementIterator(this.getDDFColumnName());
  // }
}
