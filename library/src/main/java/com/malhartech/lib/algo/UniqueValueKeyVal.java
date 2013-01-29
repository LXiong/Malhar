/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.lib.algo;

import com.malhartech.annotation.InputPortFieldAnnotation;
import com.malhartech.annotation.OutputPortFieldAnnotation;
import com.malhartech.api.DefaultInputPort;
import com.malhartech.api.DefaultOutputPort;
import com.malhartech.lib.util.BaseKeyValueOperator;
import com.malhartech.lib.util.KeyValPair;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Count unique occurrences of key,val pairs within a window, and emits one HashMap tuple. <p>
 * This is an end of window operator<br>
 * <br>
 * <b>Ports</b>:<br>
 * <b>data</b>: expects HashMap&lt;K,V&gt;<br>
 * <b>count</b>: emits HashMap&lt;HashMap&lt;K,V&gt;(1),Integer&gt;(1)<br>
 * <br>
 * <b>Properties</b>: None<br>
 * <br>
 * <b>Specific compile time checks</b>: None<br>
 * <b>Specific run time checks</b>:<br>
 * <br>
 * <b>Benchmarks</b>: Blast as many tuples as possible in inline mode<br>
 * <table border="1" cellspacing=1 cellpadding=1 summary="Benchmark table for UniqueKeyValCounter&lt;K,V&gt; operator template">
 * <tr><th>In-Bound</th><th>Out-bound</th><th>Comments</th></tr>
 * <tr><td><b>&gt; processes 8 Million K,V pairs/s</b></td><td>Emits one tuple per window</td><td>In-bound throughput
 * and number of unique K,V pairs are the main determinant of performance. Tuples are assumed to be immutable. If you use mutable tuples and have lots of keys,
 * the benchmarks may be lower</td></tr>
 * </table><br>
 * <p>
 * <b>Function Table (K=String)</b>: The order of the K,V pairs in the tuple is undeterminable
 * <table border="1" cellspacing=1 cellpadding=1 summary="Function table for UniqueKeyValCounter&lt;K,V&gt; operator template">
 * <tr><th rowspan=2>Tuple Type (api)</th><th>In-bound (process)</th><th>Out-bound (emit)</th></tr>
 * <tr><th><i>data</i>(K)</th><th><i>count</i>(HashMap&lt;HashMap&lt;K,v&gt;(1),Integer&gt;)</th></tr>
 * <tr><td>Begin Window (beginWindow())</td><td>N/A</td><td>N/A</td></tr>
 * <tr><td>Data (process())</td><td>{a=1,b=2,c=3}</td></tr>
 * <tr><td>Data (process())</td><td>{b=5}</td><td></td>/tr>
 * <tr><td>Data (process())</td><td>{c=1000}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{5ah=10}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{a=4,b=5}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{a=1,c=3}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{5ah=10}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{a=4,b=2}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{c=3,b=2}</td><td></td></tr>
 * <tr><td>End Window (endWindow())</td><td>N/A</td><td>{{a=1}=2,{5ah=10}=2,{b=5}=2,{b=2}=3,{c=1000}=1,{c=3}=3,{a=4}=2}</td></tr>
 * </table>
 * <br>
 * @author Amol Kekre <amol@malhar-inc.com>
 */
public class UniqueValueKeyVal<K,V> extends BaseKeyValueOperator<K,V>
{
  @InputPortFieldAnnotation(name = "data")
  public final transient DefaultInputPort<KeyValPair<K,V>> data = new DefaultInputPort<KeyValPair<K,V>>(this)
  {
    /**
     * Reference counts tuples
     */
    @Override
    public void process(KeyValPair<K,V> tuple)
    {
      MutableInt val = map.get(tuple.getKey());
      if (val == null) {
        val = new MutableInt();
        map.put(cloneKey(tuple.getKey()), val);
      }
      val.increment();
    }
  };
  @OutputPortFieldAnnotation(name = "count")
  public final transient DefaultOutputPort<KeyValPair<K,Integer>> count = new DefaultOutputPort<KeyValPair<K,Integer>>(this);

  /**
   * Bucket counting mechanism.
   */
  protected HashMap<K, MutableInt> map = new HashMap<K, MutableInt>();


  /**
   * Emits one HashMap as tuple
   */
  @Override
  public void endWindow()
  {
    for (Map.Entry<K,MutableInt> e: map.entrySet()) {
      count.emit(new KeyValPair(e.getKey(), e.getValue().intValue()));
    }
    clearCache();
  }

  public void clearCache()
  {
    map.clear();
  }
}