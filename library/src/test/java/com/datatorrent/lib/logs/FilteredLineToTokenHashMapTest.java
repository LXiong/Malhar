/**
 * Copyright (c) 2012-2012 Malhar, Inc. All rights reserved.
 */
package com.datatorrent.lib.logs;

import com.datatorrent.lib.logs.FilteredLineToTokenHashMap;
import com.datatorrent.lib.testbench.HashTestSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Functional tests for {@link com.datatorrent.lib.logs.FilteredLineToTokenHashMap}<p>
 *
 */
public class FilteredLineToTokenHashMapTest
{
  private static Logger log = LoggerFactory.getLogger(FilteredLineToTokenHashMapTest.class);

  /**
   * Test oper logic emits correct results
   */
  @Test
  public void testNodeProcessing()
  {

    FilteredLineToTokenHashMap oper = new FilteredLineToTokenHashMap();
    HashTestSink tokenSink = new HashTestSink();

    oper.setSplitBy(";");
    oper.setSplitTokenBy(",");
    oper.tokens.setSink(tokenSink);
    String [] filters = new String[2];
    filters[0] = "a";
    filters[1] = "c";
    oper.setFilterBy(filters);

    oper.beginWindow(0); //
    String input1 = "a,2,3;b,1,2;c,4,5,6";
    String input2 = "d";
    String input3 = "";
    int numTuples = 1000;
    for (int i = 0; i < numTuples; i++) {
      oper.data.process(input1);
      oper.data.process(input2);
      oper.data.process(input3);
    }
    oper.endWindow(); //
    Assert.assertEquals("number emitted tuples", 2, tokenSink.map.size());
    HashMap<HashMap<String, ArrayList<String>>, Object> smap = tokenSink.map;
    for (Map.Entry<HashMap<String, ArrayList<String>>, Object> e: smap.entrySet()) {
      for (Map.Entry<String, ArrayList<String>> l: e.getKey().entrySet()) {
        String key = l.getKey();
        ArrayList<String> list = l.getValue();
        Assert.assertTrue(!key.equals("b"));
        Assert.assertTrue(!key.equals("d"));
        if (key.equals("a")) {
          Assert.assertEquals("number emitted values for \"a\"", 2, list.size());
          Assert.assertEquals("first value for \"a\"", "2", list.get(0));
          Assert.assertEquals("second value for \"a\"", "3", list.get(1));
        }
        else if (key.equals("c")) {
          Assert.assertEquals("number emitted values for \"c\"", 3, list.size());
          Assert.assertEquals("first value for \"c\"", "4", list.get(0));
          Assert.assertEquals("second value for \"c\"", "5", list.get(1));
          Assert.assertEquals("second value for \"c\"", "6", list.get(2));
        }
      }
    }
  }
}