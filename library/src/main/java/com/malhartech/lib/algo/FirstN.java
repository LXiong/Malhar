/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.lib.algo;

import com.malhartech.annotation.InputPortFieldAnnotation;
import com.malhartech.annotation.OutputPortFieldAnnotation;
import com.malhartech.api.BaseOperator;
import com.malhartech.api.DefaultInputPort;
import com.malhartech.api.DefaultOutputPort;
import com.malhartech.lib.util.MutableInteger;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Takes in one stream via input port "data". Takes the first N tuples of a particular key and emits them as they come in on output port "first".<p>
 * This module is a pass through module<br>
 * <br>
 * Ports:<br>
 * <b>data</b>: expects HashMap<K, V><br>
 * <b>first</b>: emits HashMap<K, V><br>
 * <br>
 * Properties:<br>
 * <b>n</b>: Number of tuples to pass through for each key. Default value of N is 1.<br>
 * <br>
 * Compile time checks<br>
 * N if specified must be an integer<br>
 * <br>
 * Run time checks<br>
 * none<br>
 * <br>
 * <b>Benchmarks</b>: Blast as many tuples as possible in inline mode<br>
 *
 * @author amol
 */

public class FirstN<K,V> extends BaseOperator
{
  @InputPortFieldAnnotation(name="data")
  public final transient DefaultInputPort<HashMap<K, V>> data = new DefaultInputPort<HashMap<K, V>>(this)
  {
    @Override
    public void process(HashMap<K, V> tuple)
    {
      for (Map.Entry<K, V> e: tuple.entrySet()) {
        MutableInteger count = keycount.get(e.getKey());
        if (count == null) {
          count = new MutableInteger(0);
          keycount.put(e.getKey(), count);
        }
        count.value++;
        if (count.value <= n) {
          HashMap<K, V> dtuple = new HashMap<K, V>(1);
          dtuple.put(e.getKey(), e.getValue());
          first.emit(dtuple);
        }
      }
    }
  };

  @OutputPortFieldAnnotation(name="first")
  public final transient DefaultOutputPort<HashMap<K, V>> first = new DefaultOutputPort<HashMap<K, V>>(this);

  HashMap<K, MutableInteger> keycount = new HashMap<K, MutableInteger>();

  int n_default_value = 1;
  int n = n_default_value;

  public void setN(int val) {
    n = val;
  }

  @Override
  public void beginWindow()
  {
    keycount.clear();
  }
}
