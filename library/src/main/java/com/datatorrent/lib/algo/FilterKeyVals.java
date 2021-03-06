/*
 * Copyright (c) 2013 DataTorrent, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.lib.algo;

/*
 *  Copyright (c) 2012-2013 DataTorrent, Inc.
 *  All Rights Reserved.
 */


import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.annotation.InputPortFieldAnnotation;
import com.datatorrent.api.annotation.OutputPortFieldAnnotation;
import com.datatorrent.lib.util.BaseKeyOperator;

/**
 *
 * Filters the incoming stream based of specified key,val pairs, and emits those that match the filter. If
 * property "inverse" is set to "true", then all key,val pairs except those specified by in keyvals parameter are emitted<p>
 * Operator assumes that the key, val pairs are immutable objects. If this operator has to be used for mutable objects,
 * override "cloneKey()" to make copy of K, and "cloneValue()" to make copy of V.<br>
 * This is a pass through node<br>
 * <br>
 * <b>StateFull : No, </b> tuple are processed in current window. <br>
 * <b>Partitions : Yes, </b> no dependency among input tuples. <br>
 * <br>
 * <b>Ports</b>:<br>
 * <b>data</b>: expects HashMap&lt;K,V&gt;<br>
 * <b>filter</b>: emits HashMap&lt;K,V&gt;(1)<br>
 * <br>
 * <b>Properties</b>:<br>
 * <b>keyvals</b>: The keyvals is key,val pairs to pass through, rest are filtered/dropped.<br>
 * <br>
 *
 * @since 0.3.2
 */
public class FilterKeyVals<K,V> extends BaseKeyOperator<K>
{
  @InputPortFieldAnnotation(name="data")
  public final transient DefaultInputPort<HashMap<K, V>> data = new DefaultInputPort<HashMap<K, V>>()
  {
    /**
     * Processes incoming tuples one key,val at a time. Emits if at least one key makes the cut
     * By setting inverse as true, match is changed to un-matched
     */
    @Override
    public void process(HashMap<K, V> tuple)
    {
      for (Map.Entry<K, V> e: tuple.entrySet()) {
        entry.clear();
        entry.put(e.getKey(),e.getValue());
        boolean contains = keyvals.containsKey(entry);
        if ((contains && !inverse) || (!contains && inverse)) {
          HashMap<K, V> dtuple = new HashMap<K,V>(1);
          dtuple.put(cloneKey(e.getKey()), cloneValue(e.getValue()));
          filter.emit(dtuple);
        }
      }
    }
  };

  @OutputPortFieldAnnotation(name="filter")
  public final transient DefaultOutputPort<HashMap<K, V>> filter = new DefaultOutputPort<HashMap<K, V>>();

  @NotNull()
  HashMap<HashMap<K,V>,Object> keyvals = new HashMap<HashMap<K,V>,Object>();
  boolean inverse = false;
  private transient HashMap<K,V> entry = new HashMap<K,V>(1);

  /**
   * getter function for parameter inverse
   * @return inverse
   */
  public boolean getInverse() {
    return inverse;
  }

  /**
   * True means match; False means unmatched
   * @param val
   */
  public void setInverse(boolean val) {
    inverse = val;
  }

  /**
   * True means match; False means unmatched
   * @return keyvals hash
   */
  @NotNull()
  public HashMap<HashMap<K,V>,Object> getKeyVals() {
    return keyvals;
  }

  /**
   * Adds a key to the filter list
   * @param map with key,val pairs to set as filters
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setKeyVals(HashMap<K,V> map)
  {
    for (Map.Entry<K, V> e: map.entrySet()) {
      HashMap kvpair = new HashMap<K,V>(1);
      kvpair.put(cloneKey(e.getKey()), cloneValue(e.getValue()));
      keyvals.put(kvpair, null);
    }
  }

  /*
   * Clears the filter list
   */
  public void clearKeys()
  {
    keyvals.clear();
  }

  /**
   * Clones V object. By default assumes immutable object (i.e. a copy is not made). If object is mutable, override this method
   * @param v value to be cloned
   * @return returns v as is (assumes immutable object)
   */
  public V cloneValue(V v)
  {
    return v;
  }
}
