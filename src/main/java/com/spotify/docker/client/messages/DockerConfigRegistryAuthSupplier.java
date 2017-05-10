/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.docker.client.messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.ObjectMapperProvider;
import com.spotify.docker.client.messages.RegistryConfigs.RegistryConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerConfigRegistryAuthSupplier implements RegistryAuthSupplier {

  private static final Logger LOG = LoggerFactory.getLogger(DockerConfigRegistryAuthSupplier.class);

  private static final ObjectMapper MAPPER = ObjectMapperProvider.objectMapper();

  public DockerConfigRegistryAuthSupplier() {
  }

  @Override
  public RegistryAuth authFor(final String registryName) {
    try {
      return RegistryAuth.fromDockerConfig(registryName).build();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public RegistryConfigs allAuths() {
    try {
      return extractAuthJson(defaultConfigPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static RegistryConfigs extractAuthJson(final Path configPath) throws IOException {
    final JsonNode config = MAPPER.readTree(configPath.toFile());
    final JsonNode auths = config.get("auths");
    if (auths == null) {
      return RegistryConfigs.empty();
    }

    final ImmutableMap.Builder<String, RegistryConfig> mapBuilder = ImmutableMap.builder();

    final Iterator<Map.Entry<String, JsonNode>> authIterator = auths.fields();
    while (authIterator.hasNext()) {
      final Map.Entry<String, JsonNode> next = authIterator.next();
      final String registryName = next.getKey();
      final JsonNode value = next.getValue();
      final String username = value.get("username") == null ? null : value.get("username").asText();
      final String password = value.get("password") == null ? null : value.get("password").asText();
      final String email = value.get("email") == null ? null : value.get("email").asText();
      final String auth = value.get("auth") == null ? null : value.get("auth").asText();

      mapBuilder.put(registryName,
          RegistryConfig.create(registryName, username, password, email, auth));
    }

    return RegistryConfigs.create(mapBuilder.build());
  }

  private static Path defaultConfigPath() {
    final String home = System.getProperty("user.home");
    final Path dockerConfig = Paths.get(home, ".docker", "config.json");
    final Path dockerCfg = Paths.get(home, ".dockercfg");

    if (Files.exists(dockerConfig)) {
      LOG.debug("Using configfile: {}", dockerConfig);
      return dockerConfig;
    } else if (Files.exists(dockerCfg)) {
      LOG.debug("Using configfile: {} ", dockerCfg);
      return dockerCfg;
    } else {
      throw new RuntimeException(
          "Could not find a docker config. Please run 'docker login' to create one");
    }
  }
}
