/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.parquet.internal.column.columnindex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.PrimitiveComparator;
import org.apache.parquet.schema.PrimitiveType;

class BinaryColumnIndexBuilder extends ColumnIndexBuilder {
  private static class BinaryColumnIndex extends ColumnIndexBase {
    private Binary[] minValues;
    private Binary[] maxValues;

    private BinaryColumnIndex(PrimitiveType type) {
      super(type);
    }

    @Override
    ByteBuffer getMinValueAsBytes(int pageIndex) {
      return convert(minValues[pageIndex]);
    }

    @Override
    ByteBuffer getMaxValueAsBytes(int pageIndex) {
      return convert(maxValues[pageIndex]);
    }

    @Override
    String getMinValueAsString(int pageIndex) {
      return stringifier.stringify(minValues[pageIndex]);
    }

    @Override
    String getMaxValueAsString(int pageIndex) {
      return stringifier.stringify(maxValues[pageIndex]);
    }
  }

  private final List<Binary> minValues = new ArrayList<>();
  private final List<Binary> maxValues = new ArrayList<>();

  private static Binary convert(ByteBuffer buffer) {
    return Binary.fromReusedByteBuffer(buffer);
  }

  private static ByteBuffer convert(Binary value) {
    return value.toByteBuffer();
  }

  @Override
  void addMinMaxFromBytes(ByteBuffer min, ByteBuffer max) {
    minValues.add(min == null ? null : convert(min));
    maxValues.add(max == null ? null : convert(max));
  }

  @Override
  void addMinMax(Object min, Object max) {
    minValues.add((Binary) min);
    maxValues.add((Binary) max);
  }

  @Override
  ColumnIndexBase createColumnIndex(PrimitiveType type) {
    BinaryColumnIndex columnIndex = new BinaryColumnIndex(type);
    columnIndex.minValues = minValues.toArray(new Binary[minValues.size()]);
    columnIndex.maxValues = maxValues.toArray(new Binary[maxValues.size()]);
    return columnIndex;
  }

  @Override
  void clearMinMax() {
    minValues.clear();
    maxValues.clear();
  }

  @Override
  int compareMinValues(PrimitiveComparator<Binary> comparator, int index1, int index2) {
    return comparator.compare(minValues.get(index1), minValues.get(index2));
  }

  @Override
  int compareMaxValues(PrimitiveComparator<Binary> comparator, int index1, int index2) {
    return comparator.compare(maxValues.get(index1), maxValues.get(index2));
  }
}
