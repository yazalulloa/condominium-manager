package kyo.yaz.condominium.manager.core.util;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ZipUtilityTest {

  @Test
  void test() throws IOException {
    ZipUtility.zipDirectory(new File("log"), "logs.zip");

  }

}