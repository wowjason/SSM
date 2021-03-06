/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartdata.erasurecode.rawcoder;

import org.apache.hadoop.classification.InterfaceAudience;
import org.smartdata.erasurecode.ErasureCodeNative;
import org.smartdata.erasurecode.ErasureCoderOptions;

import java.nio.ByteBuffer;

/**
 * A Reed-Solomon raw decoder using Intel ISA-L library.
 */
@InterfaceAudience.Private
public class NativeRSRawDecoder extends AbstractNativeRawDecoder {

  static {
    ErasureCodeNative.checkNativeCodeLoaded();
  }

  public NativeRSRawDecoder(ErasureCoderOptions coderOptions) {
    super(coderOptions);
    initImpl(coderOptions.getNumDataUnits(), coderOptions.getNumParityUnits());
  }

  @Override
  protected void performDecodeImpl(ByteBuffer[] inputs, int[] inputOffsets,
                                   int dataLen, int[] erased,
                                   ByteBuffer[] outputs, int[] outputOffsets) {
    decodeImpl(inputs, inputOffsets, dataLen, erased, outputs, outputOffsets);
  }

  @Override
  public void release() {
    destroyImpl();
  }

  @Override
  public boolean preferDirectBuffer() {
    return true;
  }

  private native void initImpl(int numDataUnits, int numParityUnits);

  private native void decodeImpl(
          ByteBuffer[] inputs, int[] inputOffsets, int dataLen, int[] erased,
          ByteBuffer[] outputs, int[] outputOffsets);

  private native void destroyImpl();

}
