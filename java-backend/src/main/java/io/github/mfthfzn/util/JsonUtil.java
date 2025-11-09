package io.github.mfthfzn.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

public class JsonUtil {

  @Getter
  private final static ObjectMapper objectMapper = new ObjectMapper();

}
