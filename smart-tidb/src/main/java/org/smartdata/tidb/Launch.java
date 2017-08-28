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

import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

public class Launch implements Runnable {
    private final static Logger LOG = LoggerFactory.getLogger(Launch.class);
    private Pd pd;
    private Tikv tikv;
    private Tidb tidb;

    private ListenableFuture<Integer> task ;
    private ListeningExecutorService service;
    private Callable callable;
    private FutureCallback futureCallback;

    public Launch(){
        try {
            pd = (Pd) Native.loadLibrary("libpd.so", Pd.class);
            tikv= (Tikv) Native.loadLibrary("libtikv.so",Tikv.class);
            tidb = (Tidb) Native.loadLibrary("libtidb.so", Tidb.class);
        }
        catch (UnsatisfiedLinkError ex){
            LOG.error(ex.getMessage());
        }
    }

    public void run() {
        String pdArgs=new String("--data-dir=pd --log-file=logs/pd.log");
        String tikvArgs=new String("--pd=127.0.0.1:2379 --data-dir=tikv --log-file=logs/tikv.log");
//        String tidbArgs= new String("--store=tikv --path=127.0.0.1:2379 --log-file=logs/tidb.log");
        String tidbArgs= new String("--log-file=logs/tidb.log");

        PdServer pdServer=new PdServer(pdArgs,pd);
        final TikvServer tikvServer=new TikvServer(tikvArgs,tikv);
        TidbServer tidbServer=new TidbServer(tidbArgs,tidb);

        //the threads must be synchronized
        Thread pdThread=new Thread(pdServer);
        pdThread.start();

        service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        callable=new Callable<Integer>() {
            public Integer call() throws Exception {
                tikvServer.run();
                LOG.error("call");
                return 0;
            }
        };
        task = service.submit(callable);

        futureCallback=new FutureCallback() {
            public void onSuccess(Object o) {
                //System.out.println("异步处理成功,result="+o);
                LOG.error("Fail to start. Try again in onSuccess.");
                ListenableFuture<Integer> task1=service.submit(callable);
                Futures.addCallback(task1,futureCallback,service);
            }

            public void onFailure(Throwable throwable) {
                LOG.error("Fail to start. Try again in onFailure.");
                ListenableFuture<Integer> task1=service.submit(callable);
                Futures.addCallback(task1,futureCallback,service);
            }
        };

        Futures.addCallback(task, futureCallback,service);


//        Thread tikvThread=new Thread(tikvServer);
//        tikvThread.start();
        try {
            Thread.sleep(4000);
        }
        catch (InterruptedException ex){
            LOG.error(ex.getMessage());
        }
        Thread tidbThread = new Thread(tidbServer);
        tidbThread.start();

        try {
            pdThread.join();
            //tikvThread.join();
            tidbThread.join();
        }
        catch (InterruptedException ex){
            LOG.error(ex.getMessage());
        }
    }
}
