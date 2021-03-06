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
package com.datatorrent.lib.streamquery.index;

import javax.validation.constraints.NotNull;


/**
 * Abstract class to filter row by expression index.
 * Sub class will implement filter/getExpressionName functions.
 *
 * @since 0.3.4
 */
abstract public class UnaryExpression  implements Index
{
  /**
   * Column name argument for unary expression.
   */
  @NotNull
  protected String column;
  
  /**
   *  Alias name for output field.
   */
  protected String alias;

  /**
   * @param Column name argument for unary expression.
   * @param Alias name for output field.
   */
  public UnaryExpression(@NotNull String column, String alias) 
  {
    this.column = column;
  }

  public String getColumn()
  {
    return column;
  }

  public void setColumn(String column)
  {
    this.column = column;
  }

  public String getAlias()
  {
    return alias;
  }

  public void setAlias(String alias)
  {
    this.alias = alias;
  }
}
