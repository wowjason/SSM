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
package org.smartdata.tidb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TikvServer implements Runnable {
    private String args;
    private final static Logger LOG = LoggerFactory.getLogger(TikvServer.class);
    private Tikv tikv;

    public TikvServer(String args,Tikv tikv){
        this.args=args;
        this.tikv=tikv;
    }
    public void run(){
        StringBuffer strbuffer=new StringBuffer();
        strbuffer.append("TiKV");  //@ App::new("TiKV") in start.rs, "TiKV" is the flag name used for parsing
        strbuffer.append(" ");
        strbuffer.append(args);

        LOG.info("Starting TiKV..");
        tikv.startServer(strbuffer.toString());
    }
}
