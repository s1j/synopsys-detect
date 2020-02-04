/**
 * synopsys-detect
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.detect.boot;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.detect.DetectTool;
import com.synopsys.integration.detect.configuration.DetectConfiguration;
import com.synopsys.integration.detect.configuration.DetectProperty;
import com.synopsys.integration.detect.configuration.PropertyAuthority;
import com.synopsys.integration.detect.lifecycle.boot.decision.ProductDecider;
import com.synopsys.integration.detect.lifecycle.boot.decision.ProductDecision;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;

public class ProductDeciderTest {

    @Test
    public void shouldRunPolaris() {
        final File userHome = Mockito.mock(File.class);
        final DetectConfiguration detectConfiguration = polarisConfiguration("POLARIS_ACCESS_TOKEN", "access token text", "POLARIS_URL", "http://polaris.com");

        final ProductDecider productDecider = new ProductDecider();
        final DetectToolFilter detectToolFilter = Mockito.mock(DetectToolFilter.class);
        Mockito.when(detectToolFilter.shouldInclude(DetectTool.POLARIS)).thenReturn(Boolean.TRUE);
        final ProductDecision productDecision = productDecider.decide(detectConfiguration, userHome, detectToolFilter);

        Assertions.assertTrue(productDecision.getPolarisDecision().shouldRun());
    }

    @Test
    public void shouldRunPolarisWhenExcluded() {
        final File userHome = Mockito.mock(File.class);
        final DetectConfiguration detectConfiguration = polarisConfiguration("POLARIS_ACCESS_TOKEN", "access token text", "POLARIS_URL", "http://polaris.com");

        final ProductDecider productDecider = new ProductDecider();
        final DetectToolFilter detectToolFilter = Mockito.mock(DetectToolFilter.class);
        Mockito.when(detectToolFilter.shouldInclude(DetectTool.POLARIS)).thenReturn(Boolean.FALSE);
        final ProductDecision productDecision = productDecider.decide(detectConfiguration, userHome, detectToolFilter);

        Assertions.assertFalse(productDecision.getPolarisDecision().shouldRun());
    }

    @Test
    public void shouldRunBlackDuckOffline() {
        final File userHome = Mockito.mock(File.class);
        final DetectConfiguration detectConfiguration = Mockito.mock(DetectConfiguration.class);
        Mockito.when(detectConfiguration.getBooleanProperty(DetectProperty.BLACKDUCK_OFFLINE_MODE, PropertyAuthority.NONE)).thenReturn(true);

        final ProductDecider productDecider = new ProductDecider();
        final DetectToolFilter detectToolFilter = Mockito.mock(DetectToolFilter.class);
        Mockito.when(detectToolFilter.shouldInclude(DetectTool.POLARIS)).thenReturn(Boolean.TRUE);
        final ProductDecision productDecision = productDecider.decide(detectConfiguration, userHome, detectToolFilter);

        Assertions.assertTrue(productDecision.getBlackDuckDecision().shouldRun());
        Assertions.assertTrue(productDecision.getBlackDuckDecision().isOffline());
    }

    @Test
    public void shouldRunBlackDuckOnline() {
        final File userHome = Mockito.mock(File.class);
        final DetectConfiguration detectConfiguration = Mockito.mock(DetectConfiguration.class);
        Mockito.when(detectConfiguration.getProperty(DetectProperty.BLACKDUCK_URL, PropertyAuthority.NONE)).thenReturn("some-url");

        final ProductDecider productDecider = new ProductDecider();
        final DetectToolFilter detectToolFilter = Mockito.mock(DetectToolFilter.class);
        Mockito.when(detectToolFilter.shouldInclude(DetectTool.POLARIS)).thenReturn(Boolean.TRUE);
        final ProductDecision productDecision = productDecider.decide(detectConfiguration, userHome, detectToolFilter);

        Assertions.assertTrue(productDecision.getBlackDuckDecision().shouldRun());
        Assertions.assertFalse(productDecision.getBlackDuckDecision().isOffline());
    }

    @Test
    public void decidesNone() {
        final File userHome = Mockito.mock(File.class);
        final DetectConfiguration detectConfiguration = Mockito.mock(DetectConfiguration.class);

        final ProductDecider productDecider = new ProductDecider();
        final DetectToolFilter detectToolFilter = Mockito.mock(DetectToolFilter.class);
        Mockito.when(detectToolFilter.shouldInclude(DetectTool.POLARIS)).thenReturn(Boolean.TRUE);
        final ProductDecision productDecision = productDecider.decide(detectConfiguration, userHome, detectToolFilter);

        Assertions.assertFalse(productDecision.willRunAny());
    }

    private DetectConfiguration polarisConfiguration(final String... polarisKeys) {
        final Map<String, String> keyMap = new HashMap<>();
        for (int i = 0; i < polarisKeys.length; i += 2) {
            keyMap.put(polarisKeys[i], polarisKeys[i + 1]);
        }
        final DetectConfiguration detectConfiguration = Mockito.mock(DetectConfiguration.class);
        Mockito.when(detectConfiguration.getProperties(Mockito.any())).thenReturn(keyMap);
        Mockito.when(detectConfiguration.getIntegerProperty(DetectProperty.BLACKDUCK_TIMEOUT, PropertyAuthority.NONE)).thenReturn(120);

        return detectConfiguration;
    }
}