/**
 * Copyright 2016 StreamSets Inc.
 * <p>
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.stage.origin.tokafka;

import com.streamsets.pipeline.config.DataFormat;
import com.streamsets.pipeline.lib.http.HttpConfigs;
import com.streamsets.pipeline.lib.http.HttpReceiver;
import com.streamsets.pipeline.lib.http.HttpRequestFragmenter;
import com.streamsets.pipeline.lib.httpsource.AbstractHttpServerSource;
import com.streamsets.pipeline.stage.destination.kafka.KafkaTargetConfig;
import com.streamsets.pipeline.stage.destination.lib.DataGeneratorFormatConfig;

import java.util.List;

public abstract class HttpServerToKafkaSource extends AbstractHttpServerSource<HttpReceiver> {
  private final HttpRequestFragmenter fragmenter;
  private final KafkaTargetConfig kafkaConfigs;

  public HttpServerToKafkaSource(
      String uriPath,
      HttpConfigs httpConfigs,
      HttpRequestFragmenter fragmenter,
      KafkaTargetConfig kafkaConfigs,
      int kafkaMaxMessageSizeKB
  ) {
    super(httpConfigs, new HttpReceiver(uriPath,
        httpConfigs,
        fragmenter,
        new KafkaFragmentWriter(kafkaConfigs, kafkaMaxMessageSizeKB, httpConfigs.getMaxConcurrentRequests())
    ));
    this.fragmenter = fragmenter;
    this.kafkaConfigs = kafkaConfigs;

    // Although the following is not used it helps validate Kafka connection
    // set the data format to SDC_JSON
    // IMPORTANT: we can do this here safely because this is used during at init() time.
    kafkaConfigs.dataFormat = DataFormat.SDC_JSON;
    kafkaConfigs.dataGeneratorFormatConfig = new DataGeneratorFormatConfig();
  }

  @Override
  protected List<ConfigIssue> init() {
    List<ConfigIssue> issues = getReceiver().init(getContext());
    issues.addAll(getReceiver().getWriter().init(getContext()));
    if (issues.isEmpty()) {
      issues.addAll(super.init());
    }
    return issues;
  }

  @Override
  public void destroy() {
    super.destroy();
    getReceiver().getWriter().destroy();
    getReceiver().destroy();
  }

  protected KafkaTargetConfig getKafkaConfigs() {
    return kafkaConfigs;
  }
  protected HttpRequestFragmenter getFragmenter() {
    return fragmenter;
  }

}
