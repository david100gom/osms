package com.osms.monitoring.monitor.os.parser;

import java.util.Map;

public abstract class ResultParser {
  
  public abstract Map<String, Object> parse(String data);
  
}
