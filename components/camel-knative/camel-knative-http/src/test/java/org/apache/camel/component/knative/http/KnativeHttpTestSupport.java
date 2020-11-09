/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.knative.http;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.component.knative.KnativeComponent;
import org.apache.camel.component.knative.spi.CloudEvent;
import org.apache.camel.component.knative.spi.KnativeEnvironment;
import org.apache.camel.component.platform.http.PlatformHttpComponent;
import org.apache.camel.component.platform.http.PlatformHttpConstants;
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpEngine;
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpServer;
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpServerConfiguration;

public final class KnativeHttpTestSupport {
    private KnativeHttpTestSupport() {
    }

    public static KnativeComponent configureKnativeComponent(CamelContext context, CloudEvent ce, KnativeEnvironment.KnativeResource... definitions) {
        return configureKnativeComponent(context, ce, Arrays.asList(definitions));
    }

    public static KnativeComponent configureKnativeComponent(CamelContext context, CloudEvent ce, List<KnativeEnvironment.KnativeResource> definitions) {
        KnativeComponent component = context.getComponent("knative", KnativeComponent.class);
        component.setCloudEventsSpecVersion(ce.version());
        component.setEnvironment(new KnativeEnvironment(definitions));

        return component;
    }

    public static String httpAttribute(CloudEvent ce, String name) {
        return ce.mandatoryAttribute(name).http();
    }

    public static void configurePlatformHttpComponent(CamelContext camelContext, int bindPort) {
        VertxPlatformHttpServerConfiguration configuration = new VertxPlatformHttpServerConfiguration();
        configuration.setBindPort(bindPort);

        try {
            camelContext.addService(new VertxPlatformHttpServer(configuration) {
                @Override
                protected void doInit() throws Exception {
                    initializeServer();
                }
                @Override
                protected void doStart() throws Exception {
                    startServer();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PlatformHttpComponent component = new PlatformHttpComponent(camelContext);
        component.setEngine(new VertxPlatformHttpEngine());

        camelContext.getRegistry().bind(PlatformHttpConstants.PLATFORM_HTTP_COMPONENT_NAME, component);
    }
}
