package com.spotify.docker.client.messages;

import java.util.Collection;

/**
 * TBA
 */
public interface RegistryAuthSupplier {
  /**
   * return a single RegistryAuth struct for use with a specific repository
   */
  RegistryAuth authFor(String registryName);

  /**
   * When building an image (and only for this operation), the Docker API
   * wants you to send information for all of the registries that the client is
   * configured for. See
   * https://docs.docker.com/engine/api/v1.28/#operation/ImageBuild for more info.
   */
  RegistryConfigs allAuths();
  
}
