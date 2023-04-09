/*
* Copyright 2020 Expedia, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.vrbo.jarviz.service;

import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.vrbo.jarviz.config.CouplingFilterConfig;
import com.vrbo.jarviz.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsageCollector implements Collector {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // This default filter will allow all the method couplings without filtering out anything.
    private static CouplingFilterConfig DEFAULT_COUPLING_FILTER = new CouplingFilterConfig.Builder().build();

    private final Multimap<Method, MethodCoupling> methodRefMap;

    private final CouplingFilterConfig couplingFilterConfig;
    private ClassLoaderService classLoaderService;

    public UsageCollector() {
        this(DEFAULT_COUPLING_FILTER, null);
    }

    /**
     * @param couplingFilterConfig A filter to conditionally select the source and target methods couplings.
     * @param classLoaderService
     */
    public UsageCollector(final CouplingFilterConfig couplingFilterConfig, ClassLoaderService classLoaderService) {
        Objects.requireNonNull(couplingFilterConfig, "couplingFilterConfig should not be null");

        this.couplingFilterConfig = couplingFilterConfig;
        this.methodRefMap = LinkedHashMultimap.create();
        this.classLoaderService = classLoaderService;
    }

    public UsageCollector(CouplingFilterConfig filterConfig) {
        this(filterConfig, null);
    }

    @Override
    public void collectMethodCoupling(final MethodCoupling coupling) {
        if (CouplingFilterUtils.filterMethodCoupling(couplingFilterConfig, coupling)) {
            methodRefMap.put(coupling.getSource(), coupling);
        }
    }

    /**
     * Generates the efferent coupling graph for each method in the classes loaded by the class loader.
     *
     * @return The list of method couplings.
     */
    public List<MethodCoupling> getMethodCouplings() {
        return ImmutableList.copyOf(
            methodRefMap.values().stream()
                        .sorted(MethodCoupling.COMPARATOR)
                        .collect(Collectors.toList())
        );
    }
}
