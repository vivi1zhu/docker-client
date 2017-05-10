package com.spotify.docker.client.messages;

import org.junit.Before;
import org.junit.Test;

public class DockerConfigRegistryAuthSupplierTest {

  private DockerConfigRegistryAuthSupplier sut;

  @Before
  public void setup() throws Exception {
    sut = new DockerConfigRegistryAuthSupplier();
  }

  @Test
  public void testAllAuths() throws Exception {
    sut.allAuths();
  }

}