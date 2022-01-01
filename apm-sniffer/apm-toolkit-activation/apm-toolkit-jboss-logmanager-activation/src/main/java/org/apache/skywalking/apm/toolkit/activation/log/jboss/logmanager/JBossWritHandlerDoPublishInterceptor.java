/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.toolkit.activation.log.jboss.logmanager;

import java.lang.reflect.Method;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.util.MethodUtil;
import org.apache.skywalking.apm.toolkit.logging.common.log.SkyWalkingContext;
import org.jboss.logmanager.ExtLogRecord;

public class JBossWritHandlerDoPublishInterceptor implements InstanceMethodsAroundInterceptor {

    private static boolean IS_EXTLOGRECORD_PUT_MDEX_EXIST;
    private static final String EXTLOGRECORD_CLASS = "org.jboss.logmanager.ExtLogRecord";
    private static final String PUT_MDC_METHOD = "putMdc";

    static {
        IS_EXTLOGRECORD_PUT_MDEX_EXIST = MethodUtil.isMethodExist(
            JBossWritHandlerDoPublishInterceptor.class.getClassLoader(), EXTLOGRECORD_CLASS,
            PUT_MDC_METHOD
        );
    }

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments,
                             Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        if (allArguments[0] instanceof ExtLogRecord) {
            ExtLogRecord record = (ExtLogRecord) allArguments[0];
            String skyWalingContextStr = "";
            if (IS_EXTLOGRECORD_PUT_MDEX_EXIST) {
                if (ContextManager.isActive()) {
                    skyWalingContextStr = new SkyWalkingContext(
                        ContextManager.getGlobalTraceId(),
                        ContextManager.getSegmentId(),
                        ContextManager.getSpanId()
                    )
                        .toString();
                } else {
                    skyWalingContextStr = "N/A";
                }
                record.putMdc("SW_CTX", skyWalingContextStr);
            }
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments,
                              Class<?>[] argumentsTypes, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method,
                                      Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }
}
